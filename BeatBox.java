package BeatLoaf;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.util.Random;



public class BeatBox {  // implements MetaEventListener 

      JPanel mainPanel;
      ArrayList<JCheckBox> checkboxList;// Since this will use check boxes, we create an array in which to store them
      Sequencer sequencer;
      Sequence sequence;
      Sequence mySequence = null;
      Track track;
      JFrame theFrame;

      String[] instrumentNames = {"Whistle","Crash Cymbal","Open Hi-Hat","Closed Hi-Hat","Acoustic Snare","Hand Clap","Hi Bongo","Maracas","Low Conga","Cowbell","Vibraslap","High Agogo","Open Hi Conga","High Tom","Low-mid Tom","Bass Drum"};// Names of the instruments for the sake of the human using this
      int[] instruments = {72,49,46,42,38,39,60,70,64,56,58,67,63,50,47,35};// Numbers of the "Keys" the MIDI sequencer uses while in the drum "instrument"; each happens to already be mapped to a different percussion noise
    

      public static void main (String[] args) {// Building the GUI and the buttons
        new BeatBox().buildGUI();
      }

      public void buildGUI() {
          theFrame = new JFrame("BeatLoaf");
          theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          BorderLayout layout = new BorderLayout();
          JPanel background = new JPanel(layout);
          background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Gave a basic border around the buttons and the edge of the app

          checkboxList = new ArrayList<JCheckBox>();// Making an array to hold all 16*16=256 boxes
          Box buttonBox = new Box(BoxLayout.Y_AXIS);// it's a box full of buttons

          JButton start = new JButton("Start");// Click this to drop the beat
          start.addActionListener(new MyStartListener());
          buttonBox.add(start);
          
          JButton stop = new JButton("Stop");// Click this to stop the beat
          stop.addActionListener(new MyStopListener());
          buttonBox.add(stop);

          JButton upTempo = new JButton("Tempo Up");// New button to increase tempo
          upTempo.addActionListener(new MyUpTempoListener());
          buttonBox.add(upTempo);

           JButton downTempo = new JButton("Tempo Down");// New button to slow the tempo
          downTempo.addActionListener(new MyDownTempoListener());
          buttonBox.add(downTempo);

          JButton randomBeat = new JButton("Randomize");  // New button which will randomly check about half of the boxes
          randomBeat.addActionListener(new MyRandomizeListener());
          buttonBox.add(randomBeat);

          JButton resetIt = new JButton("Reset All");     // New button which will stop the song and clear all of the boxes
          resetIt.addActionListener(new MyResetAllListener());
          buttonBox.add(resetIt);

          Box nameBox = new Box(BoxLayout.Y_AXIS); // A box of names; those names are in the instrument array
          for (int i = 0; i < 16; i++) {
              nameBox.add(new Label(instrumentNames[i]));
          }
        
          background.add(BorderLayout.EAST, buttonBox); // Control buttons will be on the right of the checkboxes
          background.add(BorderLayout.WEST, nameBox); // Names of the instruments will be on the left of the checkboxes

          theFrame.getContentPane().add(background); // Making a window pane so the thing is visible
          
          GridLayout grid = new GridLayout(16,16);// Making a place to hold 256 check boxes
          grid.setVgap(1);
          grid.setHgap(2);
          mainPanel = new JPanel(grid);
          background.add(BorderLayout.CENTER, mainPanel);


          for (int i = 0; i < 256; i++) {// Populate the grid with 16*16=256 checkboxes and starting them off as unchecked
                JCheckBox c = new JCheckBox();
                c.setSelected(false);
                checkboxList.add(c);
                mainPanel.add(c);            
          } 
//-=-=-=-=-Seting up and initializing MIDI-=-=-=-=-
          setUpMidi(); 

          theFrame.setBounds(50,50,300,300);
          theFrame.pack();
          theFrame.setVisible(true);
        }  


     public void setUpMidi() {
       try {
        sequencer = MidiSystem.getSequencer();
        sequencer.open();
        // sequencer.addMetaEventListener(this);
        sequence = new Sequence(Sequence.PPQ,4);
        track = sequence.createTrack();
        sequencer.setTempoInBPM(120);
        
       } catch(Exception e) {e.printStackTrace();}
    }  
//-=-=-=-=-Done setting up MIDI section-=-=-=-=-


     public void buildTrackAndStart() { //Allows for more than one instrument to play at a time by holding the vertical stack as keys
        int[] trackList = null;
     
        sequence.deleteTrack(track); // Get rid of the old track
        track = sequence.createTrack(); // Lay down a fresh track
        

      for (int i = 0; i < 16; i++) { // We've got 16 rows, so let's add 16 beats (at 120 Beats Per Minute that gets us an 8-second track)
         trackList = new int[16];

         int key = instruments[i]; // When the user checks a box, this determines which "instrument" is selected by picking it from the array of instrument numbers

         for (int j = 0; j < 16; j++ ) { // For each beat in the row, this picks which instrument is selected
               JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
               
              if ( jc.isSelected()) { // Checks if a given checkbox is selected
                 trackList[j] = key;  // If it is selected, it puts the key value in the array of things to play during that beat by setting it to 1
              } else {                //
                 trackList[j] = 0;    // And if it's not supposed to play, it sets it to 0 in that beat's slot
              }       
          } 

       makeTracks(trackList); // Make a track for each beat and add the instruments to it
     } 

     track.add(makeEvent(192,9,1,0,15)); // - It's set to 15 because otherwise it would get to the last checked box and loop early 
               
   
      
       try {
           
           sequencer.setSequence(sequence);  
           sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY); // This keeps the beat bumping all night long        
           sequencer.start(); // This begins the beat
           sequencer.setTempoInBPM(120); // Sets initial tempo to 120 beats per minute
       } catch(Exception e) {e.printStackTrace();} 
 
      }  
            
//-=-=-=-=- These are inner classes for the buttons so they do more than look pretty-=-=-=-=-  
       
      public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
             buildTrackAndStart();
         }
      }

    public class MyStopListener implements ActionListener {
       public void actionPerformed(ActionEvent a) {
           sequencer.stop();
       }
    }

    public class MyUpTempoListener implements ActionListener {
       public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.05)); // Increases tempo by 5%
       }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .95)); // Decreases tempo by 5%
        }
    }

    public class MyRandomizeListener implements ActionListener {    // My hands are cold and I'm getting too old to click 256 little boxes, but I still like dropping hot beats, so I made a random box clicker
       public void actionPerformed(ActionEvent a) {
         for (int i = 0; i < 256; i++) {
            Random randomize = new Random();
            int rand=randomize.nextInt(100); 
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (rand%5==0) // Originally had set this to rand%2 but that meant way too many boxes were checked for my tastes; dialed it down so ~20% of boxes will be checked
            {
               check.setSelected(true);
            }
            else
            {
               check.setSelected(false); // Added this in to clear boxes because otherwise a few clicks of "Randomize" would fill the entire checklist
            }
          }

       

       }  
     } 



    public class MyResetAllListener implements ActionListener {  // Un-clicking boxes is as annoying as clicking boxes, so here's a clear button. You're welcome.
        public void actionPerformed(ActionEvent a) {
          for (int i = 0; i < 256; i++) {
             JCheckBox check = (JCheckBox) checkboxList.get(i);
             {
                check.setSelected(false);
             }
             sequencer.stop();
         } 
      }  
  } 


//-=-=-=-=-To turn all this hot data into hot beats, this section sees which keys (instruments) are being played and puts them in an array of beats. Each instrument is represented in the array either with their number or as a 0. The 0 means don't play that instrument; the number means make an event and add it to the track.-=-=-=-=-    

     public void makeTracks(int[] list) {

         for (int i = 0; i < 16; i++) {
           int key = list[i];

           if (key != 0) {
               track.add(makeEvent(144,9,key, 100, i));    // This is how MIDI commands work; it's the message type, the channel (MIDI instrument), the note to play, and the duration (or how long)
               track.add(makeEvent(128,9,key, 100, i + 1));// More comments in the next section
           }
         }
      }
// How MIDI accepts commands: comd can be 144 to start playing, 128 to stop, or 192 to change instruments.
// chan is the channel. This is another word for MIDI instrument. I have 9 selected because that's the set of percussion instruments. If you put in 1, you'll get the sounds of a cat walking across a piano instead of an octopus playing drums.
// 3rd argument is which "note" to play. This corresponds to which key on an actual MIDI keyboard you'd hit to make the noise, between number 0 and 127.
// 4th arguemnt is the duration. 0 would be too fast to hear, so 100 is a nice middle ground.
// Last is the tick, which is the incremented result of the beat (figured out from the earlier beats per minute equation) so that tells the player when to play the next beat

     public  MidiEvent makeEvent(int comd, int chan, int note, int duration, int tick) {
          MidiEvent event = null;
          try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, note, duration);
            event = new MidiEvent(a, tick);
            
            }catch(Exception e) { }
          return event;
       }


   }

        
             
          
          
          