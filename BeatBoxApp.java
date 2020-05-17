import javax.sound.midi.*;
import javax.sound.midi.Sequence;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class BeatBoxApp {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer player;
    Sequence CD;
    Track track;
    JFrame frame;
    String[] instrumentNames = {"Bass Drums","Closed Hi-Hat","Open Hi - Hat","Acoustic Snare","Crash Cymbal","Hand Conga","High Tom","HI Bongo","Maracas","Whistle","Low Conga","Cowbell","Vibraslap","Low-Mid Tom","High Agogo","Open Hi conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63}; 
    public static void main(String[] args) {
        BeatBoxApp miniApp = new BeatBoxApp();
        miniApp.BuildGUI();
     
    }
    public void BuildGUI(){
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel panel = new JPanel(layout);
        //aesthetic purposes, give space between panel and edges 
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,10));
        
        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop= new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int  i = 0; i < 16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }
        panel.add(BorderLayout.EAST,buttonBox);
        panel.add(BorderLayout.WEST,nameBox);
        frame.getContentPane().add(panel);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        panel.add(BorderLayout.CENTER,mainPanel);
// make checkboxes set them to false ( not checked) and add them to rray list 
// and GUI panel 
        for(int i = 0; i < 256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        }
        setUpMidi();
        frame.setBounds(50,50,300,300);
        frame.pack();
        frame.setVisible(true);

    }
    public void setUpMidi(){
        try{
            // sequencer object is the device that dequences MIDI info into a song 
            player = MidiSystem.getSequencer();
            player.open();
            CD = new Sequence(Sequence.PPQ, 4);
            track = CD.createTrack();
            player.setTempoInBPM(120);
        }
        catch (Exception ex){ 
            ex.printStackTrace();
         }

    }  
    public void buildTrackAndStart(){
        // hold values for one element ( across all 16 beats )
        int [] trackList = null;
        // get rid of old track 
        CD.deleteTrack(track); 
        track = CD.createTrack();

        // do this for each of the 16 rows  ( Bass, , congo , etc ..)
        for(int i = 0; i < 16; i ++)
        {
            trackList = new int[16];
        // set the key that represent each instrument ( the actual MIDI #)
            int key = instruments[i];
        
        // for each of the beats for this row 
        for ( int j = 0; j < 16; j++)
        {
            // give each check box unique # ( number them from 1 - 256)
            JCheckBox jc = ( JCheckBox) checkboxList.get(j+(16*i));
            if( jc.isSelected()){
            // if check box selected set key value to play the instrument
                trackList[j] = key;
            }
            else 
            {
                trackList[j] = 0;
            }
        }
        // for this instrument, and for all 16 beats, amke events and add them to track 
        makeTracks(trackList);
        track.add(makeEvent(176, 1, 127, 0, 16));
    }
    // since it goes from 0 - 15, we have to make sure there is a beat at 16
    track.add(makeEvent(192, 9, 1, 0, 15));
    try{
        player.setSequence(CD);
        player.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
        player.start();
        player.setTempoInBPM(120);
    }
    catch(Exception e){ e.printStackTrace();}

    }
    public class MyStartListener implements ActionListener {
        public void actionPerformed (ActionEvent a){
            // try {
            buildTrackAndStart();
            // }
            // catch (Exception e){ }
        }
    }
    public class MyStopListener implements ActionListener {
        public void actionPerformed (ActionEvent a){
            player.stop();
        }
    }
    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a){
            float tempoFactor = player.getTempoFactor();
            player.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }
    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed (ActionEvent a){
            float tempoFactor = player.getTempoFactor();
            player.setTempoFactor((float)(tempoFactor * .97));
        }
    }
 
    public static MidiEvent makeEvent (int cmd,int chan,int one,int two,int tick){
        MidiEvent event = null;
        try{
        ShortMessage first = new ShortMessage();
        first.setMessage(cmd, chan,one,two);
        event = new MidiEvent(first, tick);
        }
            catch (Exception e){ }
     return event;
         
    }
    public void makeTracks (int[] list){
        for(int i = 0; i < 16; i++){
            int key = list[i];
            if(key !=0){
                // make note ON and OFF
                track.add(makeEvent(144, 9, key, 100, i+1));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }
    
}//close outerclass