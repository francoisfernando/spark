package nl.ypmania.env;

import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import nl.ypmania.fs20.Command;
import nl.ypmania.fs20.Dimmer;
import nl.ypmania.fs20.FS20Address;
import nl.ypmania.fs20.FS20MotionSensor;
import nl.ypmania.fs20.FS20Packet;
import nl.ypmania.fs20.FS20Route;
import nl.ypmania.fs20.FS20Service;
import nl.ypmania.fs20.Switch;
import nl.ypmania.rf12.Doorbell;
import nl.ypmania.rf12.ElectricityMeter;
import nl.ypmania.rf12.HumidityRoomSensor;
import nl.ypmania.rgb.LampColor;
import nl.ypmania.rgb.RGBLamp;
import nl.ypmania.visonic.DoorSensor;
import nl.ypmania.visonic.SensorDTO;
import nl.ypmania.visonic.VisonicAddress;
import nl.ypmania.visonic.VisonicMotionSensor;
import nl.ypmania.xbmc.XBMCService;
import nl.ypmania.xbmc.XBMCService.State;
import nl.ypmania.zoneminder.ZoneMinderService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/home")
@Component
public class Home extends Environment {
  private static final Logger log = LoggerFactory.getLogger(Home.class);
  
  public static final String ALARM_NIGHT = "NIGHT";
  public static final String ALARM_ALL = "ALL";
  
  private static final int HOUSE = 12341234;
  private static final int BUTTONS = 12344444;
  private static final int SENSORS = 12343333;
  
  private static final FS20Address MASTER = new FS20Address(HOUSE, 4444);
  private static final FS20Address ALL_LIGHTS = new FS20Address(HOUSE, 4411);
  private static final FS20Address LIVING_ROOM = new FS20Address(HOUSE, 1144);
  private static final FS20Address BRYGGERS = new FS20Address(HOUSE, 1244);
  private static final FS20Address BEDROOM = new FS20Address(HOUSE, 1344);
  private static final FS20Address DININGROOM = new FS20Address(HOUSE, 1444);
  private static final FS20Address CARPORT = new FS20Address(HOUSE, 3144);
  
  private static final VisonicAddress BRYGGERS_DOOR = new VisonicAddress(0x03, 0x19, 0x15);
  private static final VisonicAddress MAIN_DOOR = new VisonicAddress(0x02, 0xcf, 0xd5);
  private static final VisonicAddress LIVING_ROOM_SENSOR = new VisonicAddress(0x02, 0xc4, 0x83);
  private static final VisonicAddress BEDROOM_SENSOR = new VisonicAddress(0x04, 0xff, 0x1d);
  private static final FS20Address CARPORT_SPOTS = new FS20Address(HOUSE, 3111);
  
  private @Autowired FS20Service fs20Service;
  private @Autowired SFX sfx;
  private @Autowired XBMCService xbmcService;
  private @Autowired ZoneMinderService zoneMinderService;
  
  private Settings settings = new Settings();
  
  private DateTime doorOpenNotified = null;
  Zone drivewayLeft = new Zone(this, "Driveway, l");
  Zone drivewayRight = new Zone(this, "Driveway, r");
  Zone carport = new Zone(this, "Carport");
  Zone outside = new Zone(this, "Outside", drivewayLeft, drivewayRight, carport);
  Zone bryggers = new Zone(this, "Bryggers");
  Zone entree = new Zone(this, "Entree");
  Zone guestRoom = new Zone(this, "Guestroom");
  Zone office = new Zone(this, "Office");
  Zone kitchen = new Zone(this, "Kitchen");
  Zone studio = new Zone(this, "Studio");
  Zone livingRoom = new Zone(this, "Livingroom");
  Zone bedRoom = new Zone(this, "Bedroom");
  Zone mainBathRoom = new Zone(this, "Main bathroom");
  Zone day = new Zone(this, "Day", bryggers, entree, guestRoom, office, kitchen, studio, livingRoom);
  Zone night = new Zone(this, "Night", bedRoom, mainBathRoom);
  Zone inside = new Zone(this, "Inside", day, night);
  Zone home = new Zone(this, "Area", inside, outside);
  
  Dimmer carportSpots = new Dimmer(carport, "Spots", CARPORT_SPOTS);
  Switch carportFlood = new Switch(carport, "Floodlight", new FS20Address(HOUSE, 3112), MASTER, ALL_LIGHTS, CARPORT);
  Switch bryggersSpots = new Switch(bryggers, "Ceiling", new FS20Address(HOUSE, 1211), MASTER, ALL_LIGHTS, BRYGGERS);
  
  Dimmer livingRoomCeiling = new Dimmer(livingRoom, "Ceiling", new FS20Address(HOUSE, 1111), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomTableLamp = new Switch(livingRoom, "Table lamp", new FS20Address(HOUSE, 1112), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomReadingLamp = new Switch(livingRoom, "Reading lamp", new FS20Address(HOUSE, 1113), MASTER, ALL_LIGHTS, LIVING_ROOM);
  Switch livingRoomCornerLamp = new Switch(livingRoom, "Corner lamp", new FS20Address(HOUSE, 1114), MASTER, ALL_LIGHTS, LIVING_ROOM);
  
  Switch xmasLights = new Switch(kitchen, "X-Mas Lights", new FS20Address(HOUSE, 2211));
  
  Switch rgbLamp = new Switch(livingRoom, "RGB Lamp", new FS20Address(HOUSE, 1411), DININGROOM);
  RGBLamp kitchenLeds = new RGBLamp(kitchen, "Kitchen LEDs", (int)'L', (int)'K');
  
  DateTime lastDoorbellEmail = null;
  private final Doorbell doorbell = new Doorbell(carport, 'D','B') {
    @Override protected void ring() {
      zoneMinderService.triggerEvent(1, 30, 10, "Doorbell", "Doorbell");
      if (!settings.isMuteDoorbell()) {
        sfx.play("doorbell.01.wav");            
      }
      if (inside.noActionSinceMinutes(30)) {
        if (lastDoorbellEmail == null || lastDoorbellEmail.plusMinutes(3).isBeforeNow()) {
          getEmailService().sendMail("Doorbell", "Somebody just rang the doorbell, but it seems nobody is home.");
          lastDoorbellEmail = DateTime.now();
        }
      }
    }
  };
  
  private void handleOpenDoor() {
    boolean obvious = guestRoom.actionSinceSeconds(15) || kitchen.actionSinceSeconds(15) || office.actionSinceSeconds(15);
    if (!settings.isMuteDoors() && !obvious) {
      doorOpenNotified = DateTime.now();
      sfx.play("tngchime.wav");         
      getTimer().schedule(new TimerTask(){
        @Override
        public void run() {
          doorOpenNotified = null;
        }
      }, 60000);
    }
  }
  
  private void handleCloseDoor() {
    if (doorOpenNotified != null) {
      if (DateTime.now().isAfter(doorOpenNotified.plusSeconds(30))) {
        sfx.play("brdgbtn1.wav");        
      }
    }
    
    doorOpenNotified = null;
  }
  
  DoorSensor bryggersDoor = new DoorSensor(bryggers, "Bryggers door", BRYGGERS_DOOR) {
    protected void opened() {
      handleOpenDoor();
      if (isDark()) {
        bryggersSpots.timedOn(300);
        carportFlood.timedOn(120);
        carportSpots.timedOn(120);                        
      }
    }
    protected void closed() {
      handleCloseDoor();
    }        
  };
  
  @PostConstruct
  public void started() {
    
    setReceivers(
      livingRoomCeiling,  
      livingRoomTableLamp,  
      livingRoomReadingLamp,  
      livingRoomCornerLamp,
      
      bryggersSpots,
      kitchenLeds,
      new Dimmer(entree, "Guest bathroom", new FS20Address(HOUSE, 1212), MASTER, ALL_LIGHTS, BRYGGERS),
      
      new Dimmer(bedRoom, "Cupboards", new FS20Address(HOUSE, 1311), MASTER, ALL_LIGHTS, BEDROOM),
      new Switch(bedRoom, "LED strip", new FS20Address(HOUSE, 1312), MASTER, ALL_LIGHTS, BEDROOM),
      
      rgbLamp,
      carportSpots,
      carportFlood,
      xmasLights,
      
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.OFF) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          livingRoomCeiling.onFull();
          livingRoomReadingLamp.onFull();
          livingRoomTableLamp.off();
          livingRoomCornerLamp.off();
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1111), Command.ON_PREVIOUS) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          livingRoomCeiling.dim(1);
          livingRoomTableLamp.onFull();
          livingRoomCornerLamp.onFull();
          livingRoomReadingLamp.off();
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.OFF) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.ON_PREVIOUS));
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.OFF));
          livingRoomReadingLamp.off();
          rgbLamp.off();
        }
      },
      new FS20Route(new FS20Address(BUTTONS, 1112), Command.ON_PREVIOUS) {
        protected void handle() {
          livingRoom.event(ZoneEvent.buttonPressed());
          fs20Service.queueFS20(new FS20Packet (BEDROOM, Command.OFF));
          fs20Service.queueFS20(new FS20Packet (LIVING_ROOM, Command.OFF));
          livingRoomReadingLamp.off();
          rgbLamp.off();
        }
      },
      new FS20MotionSensor(drivewayLeft, "Driveway, left side", new FS20Address(SENSORS, 3111)) {
        protected void motion() {
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(180);
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20MotionSensor(drivewayRight, "Driveway, right side", new FS20Address(SENSORS, 3112)) {
        protected void motion() {
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(180);
            carportSpots.timedOn(180);            
          }
        }
      },
      new FS20MotionSensor(carport, "Carport", new FS20Address(SENSORS, 3113)) {
        protected void motion() {
          zoneMinderService.triggerEvent(1, 30, 10, "CarportMotion", "Motion on carport");
          if (!settings.isMuteMotion() && 
              (bryggersDoor.isClosed() || bryggersDoor.isOpenAtLeastSeconds(60))) {
            sfx.play("tngchime.wav");
          }
          if (isDark() && !settings.isNoAutoLightsCarport()) {
            carportFlood.timedOn(300);
            carportSpots.timedOn(300);            
          }
        }
      },
      new VisonicMotionSensor(guestRoom, "Guestroom", new VisonicAddress(0x03, 0x04, 0x83)),
      new VisonicMotionSensor(kitchen, "Kitchen", new VisonicAddress(0x04, 0x05, 0x03)) {
        protected void motion() {
          if (!settings.isNoAutoLightsKitchen()) {
            xmasLights.timedOn(30 * 60);            
          }
          log.debug("Considering turning on kitchen={}, dark={}", kitchenLeds.isOn(), isDark());
          if (kitchenLeds.isOn()) {
            kitchenLeds.timedOn(1200);
          } else
          if (isDark() && !settings.isNoAutoLightsKitchen()) {
            if (!kitchenLeds.isOn()) {
              kitchenLeds.timedOn(new LampColor(1, 1, 1, 1), 1200);              
            }
          }
        }
      },
      new VisonicMotionSensor(office, "Office", new VisonicAddress(0x01, 0xc4, 0x83)),
      new VisonicMotionSensor(bedRoom, "Bedroom", BEDROOM_SENSOR),
      new VisonicMotionSensor(studio, "Studio", new VisonicAddress(0x01, 0x84, 0x83)),
      new VisonicMotionSensor(livingRoom, "Living room", LIVING_ROOM_SENSOR) {
        protected void motion() {
          if (isDark()) {
            log.debug("Considering turning on living room. Current brightness {}, timed: {}", 
                livingRoomCeiling.getBrightness(), livingRoomCeiling.isTimedOn());
            if (livingRoomCeiling.getBrightness() == 0) {
              if (settings.isNoAutoLightsLiving()) {
                log.debug("Not turning on, disabled in settings.");
              } else {                
                livingRoomCeiling.timedDim(1, 900);
              }
            } else if (livingRoomCeiling.isTimedOn()){
              livingRoomCeiling.timedDim(livingRoomCeiling.getBrightness(), 900);
            }
          }
        }
      },
      new DoorSensor(entree, "Main door", MAIN_DOOR) {
        protected void opened() {
          handleOpenDoor();
        }
        protected void closed() {
          handleCloseDoor();
        }
      },
      bryggersDoor,
      
      doorbell,
      
//      new RoomSensor(bryggers, "Bryggers", (int)'1'),
      new HumidityRoomSensor(livingRoom, "Stue", "Stue_H", (int)'2'),
      new HumidityRoomSensor(bedRoom, "Bedroom_T", "Bedroom_H", (int)'4'),
      new HumidityRoomSensor(studio, null, null, (int)'5'),
      new HumidityRoomSensor(guestRoom, null, null, (int)'6'),
      new ElectricityMeter(bryggers, (int)'1', "El_Power", "El_Energy"),
      
      new RGBLamp(livingRoom, "Plantlamp", (int)'R', (int)'G'),
      new RGBLamp(bedRoom, "Sleepstrip", (int)'L', (int)'2'),
      
      new Dimmer(mainBathRoom, "Spots", new FS20Address(HOUSE, 2111), new FS20Address(HOUSE, 2144), ALL_LIGHTS, MASTER)
    );
    
    xbmcService.setLocation(livingRoom);
    xbmcService.on(State.PLAYING, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.dim(1);
          livingRoomTableLamp.onFull();
          livingRoomCornerLamp.onFull();
          livingRoomReadingLamp.off();                  
        }
      }      
    });
    xbmcService.on(State.PAUSED, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.dim(8);
        }
      }      
    });
    xbmcService.on(State.STOPPED, new Runnable() {
      public void run() {
        if (isDark()) {
          livingRoomCeiling.timedDim(8, 900);
          livingRoomCornerLamp.off();
          livingRoomTableLamp.off();
        }
      }      
    });
    register(new TimedTask(new FixedTime(7, 00).duringWeekendPlusHours(2), SUNRISE.plusHours(1)) {
      @Override
      protected void start(long duration) {
        bryggersSpots.timedOnMillis(duration);
      }
    });
    register(new TimedTask(SUNSET.plusHours(-2), new FixedTime(23,00)) {
      @Override
      protected void start(long duration) {
        bryggersSpots.timedOnMillis(duration);
      }
    });
    register(new TimedTask(new FixedTime(7, 00), SUNRISE.plusHours(1)) {
      @Override
      protected void start(long duration) {
        rgbLamp.timedOnMillis(duration);
      }
    });
    register(new TimedTask(SUNSET.plusHours(-1), new FixedTime(22,00)) {
      @Override
      protected void start(long duration) {
        rgbLamp.timedOnMillis(duration);
      }
    });
    new LightWatchdog(this, bedRoom, livingRoom).ignore(rgbLamp);
  }
  
  @Override
  public boolean isAlarmArmed(Zone zone) {
    String alarm = settings.getAlarmMode();
    if (alarm != null) {
      return zone.getName().equalsIgnoreCase(alarm);
    } else
      return false;
  }
  
  @Path("zone")
  @GET
  public Zone getZone() {
    return home;
  }

  @Path("settings")
  @GET
  public synchronized Settings getSettings() {
    return settings;
  }
  
  @Path("settings")
  @PUT
  public synchronized void setSettings(Settings settings) {
    this.settings = settings;
  }
  
  @Path("doorbell")
  @GET
  public SensorDTO getDoorbell() {
    return new SensorDTO(doorbell);
  }
}
