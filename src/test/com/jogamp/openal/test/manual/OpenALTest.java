package com.jogamp.openal.test.manual;

/**
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in the
 * design, construction, operation or maintenance of any nuclear facility.
 */
import com.jogamp.common.nio.Buffers;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCcontext;
import com.jogamp.openal.ALCdevice;
import com.jogamp.openal.ALFactory;
import java.io.IOException;
import java.nio.*;

import com.jogamp.openal.eax.*;
import com.jogamp.openal.test.resources.ResourceLocation;
import com.jogamp.openal.util.*;
import java.io.InputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Athomas Goldberg
 * @author Michael Bien
 */
public class OpenALTest {
    public static void main(String[] args) throws InterruptedException, UnsupportedAudioFileException, IOException {
        ALC alc = ALFactory.getALC();
        ALCdevice device = alc.alcOpenDevice(null);
        ALCcontext context = alc.alcCreateContext(device, null);
        alc.alcMakeContextCurrent(context);
        AL al = ALFactory.getAL();
        
        System.out.println("devices:");
        String[] devices = alc.alcGetDeviceSpecifiers();
        for (String name : devices) {
            System.out.println("    "+name);
        }
        System.out.println("capture devices:");
        devices = alc.alcGetCaptureDeviceSpecifiers();
        for (String name : devices) {
            System.out.println("    "+name);
        }


        boolean eaxPresent = al.alIsExtensionPresent("EAX2.0");
        EAX eax = ( eaxPresent ) ? EAXFactory.getEAX() : null;
        System.err.println("EAX present:" + eaxPresent + ", EAX retrieved: "+ (null != eax));

        int[] buffers = new int[1];
        al.alGenBuffers(1, buffers, 0);

        WAVData wd = WAVLoader.loadFromStream(ResourceLocation.getTestStream0());
        al.alBufferData(buffers[0], wd.format, wd.data, wd.size, wd.freq);

        int[] sources = new int[1];
        al.alGenSources(1, sources, 0);
        al.alSourcei(sources[0], AL.AL_BUFFER, buffers[0]);

        int[] loopArray = new int[1];
        al.alGetSourcei(sources[0], AL.AL_LOOPING, loopArray, 0);
        System.err.println("Looping 1: " + (loopArray[0] == AL.AL_TRUE));

        int[] loopBuffer = new int[1];
        al.alGetSourcei(sources[0], AL.AL_LOOPING, loopBuffer, 0);
        System.err.println("Looping 2: " + (loopBuffer[0] == AL.AL_TRUE));

        if (eaxPresent && null!=eax) {
            IntBuffer env = Buffers.newDirectIntBuffer(1);
            env.put(EAX.EAX_ENVIRONMENT_BATHROOM);
            eax.setListenerProperty(EAX.DSPROPERTY_EAXLISTENER_ENVIRONMENT, env);
        }

        al.alSourcePlay(sources[0]);

        Thread.sleep(5000);

        al.alSource3f(sources[0], AL.AL_POSITION, 2f, 2f, 2f);

        Thread.sleep(5000);

        al.alListener3f(AL.AL_POSITION, 3f, 3f, 3f);

        Thread.sleep(5000);

        al.alSource3f(sources[0], AL.AL_POSITION, 0, 0, 0);

        Thread.sleep(10000);

        al.alSourceStop(sources[0]);
        al.alDeleteSources(1, sources, 0);
        alc.alcDestroyContext(context);
        alc.alcCloseDevice(device);
    }
}
