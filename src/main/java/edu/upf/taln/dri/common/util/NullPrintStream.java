/*
 * ******************************************************************************************************
 * Dr. Inventor Text Mining Framework Java Library
 * 
 * This code has been developed by the Natural Language Processing Group of the
 * Universitat Pompeu Fabra in the context of the FP7 European Project Dr. Inventor
 * Call: FP7-ICT-2013.8.1 - Agreement No: 611383
 * 
 * Dr. Inventor Text Mining Framework Java Library is available under an open licence, GPLv3, for non-commercial applications.
 * ******************************************************************************************************
 */
package edu.upf.taln.dri.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Stream redirection utility
 * 
 *
 */
public class NullPrintStream extends PrintStream {

  public NullPrintStream() {
    super(new NullByteArrayOutputStream());
  }

  private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

    @Override
    public void write(int b) {
      // do nothing
    }

    @Override
    public void write(byte[] b, int off, int len) {
      // do nothing
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
      // do nothing
    }

  }

}
