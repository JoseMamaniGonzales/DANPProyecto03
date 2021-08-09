package com.example.danpproyecto03;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

import java.util.LinkedList;
public class DetectorThread extends Thread{
    private RecorderThread grabadora;
    private WaveHeader waveHeader;
    private WhistleApi silbidoApi;
    private volatile Thread _thread;
    private LinkedList<Boolean> listaResultados = new LinkedList<Boolean>();
    private int numWhistles;
    private int whistleCheckLength = 3;
    private int whistlePassScore = 3;

    private DetectarSonidoListener detectarSonidoListener;

    public DetectorThread(RecorderThread grabadora){
        this.grabadora = grabadora;
        AudioRecord audioRecord = grabadora.getGrabadorAudio();

        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT){
            bitsPerSample = 16;
        }
        else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
            bitsPerSample = 8;
        }

        int channel = 0;
        // la detección de silbatos solo admite canales mono
        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO){
            channel = 1;
        }
        waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());
        silbidoApi = new WhistleApi(waveHeader);
    }
    private void initBuffer() {
        numWhistles = 0;
        listaResultados.clear();
        for (int i = 0; i < whistleCheckLength; i++) {
            listaResultados.add(false);
        }
    }
    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection(){
        _thread = null;
    }

    public void run() {
        try {
            byte[] buffer;
            initBuffer();

            Thread thisThread = Thread.currentThread();
            while (_thread == thisThread) {
                // sonido detectado
                buffer = grabadora.getFrameBytes();
                // analisador de audio
                if (buffer != null) {
                    // sonido detectado
                    // detección de silbidos
                    //System.out.println("*Whistle:");
                    boolean isWhistle = silbidoApi.isWhistle(buffer);
                    if (listaResultados.getFirst()) {
                        numWhistles--;
                    }

                    listaResultados.removeFirst();
                    listaResultados.add(isWhistle);

                    if (isWhistle) {
                        numWhistles++;
                    }

                    if (numWhistles >= whistlePassScore) {
                        // Limpiar buffer
                        initBuffer();
                        onWhistleDetected();
                    }
                    // fin de detección de silbatos
                }
                else{
                    // no sound detected
                    if (listaResultados.getFirst()) {
                        numWhistles--;
                    }
                    listaResultados.removeFirst();
                    listaResultados.add(false);
                }
                // terminar el analista de audio
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWhistleDetected(){
        if (detectarSonidoListener != null){
            detectarSonidoListener.onWhistleDetected();
        }
    }

    public void setDetectarSonidoListener(DetectarSonidoListener listener){
        detectarSonidoListener = listener;
    }
}