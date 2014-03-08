/*
 * #%L
 * ImageJ OPS: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2014 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.ops.tests;

import static org.junit.Assert.assertTrue;
import imagej.ops.OpService;
import imagej.ops.UnaryFunction;
import imagej.ops.slicer.SlicingService;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;

public class SlicingIterableIntervalTests extends AbstractOpTest {

	private Context context;

	private OpService ops;

	private Img<ByteType> in;

	private SlicingService slicerService;

	private ArrayImg<ByteType, ByteArray> out;

	@Override
	@Before
	public void setUp() {
		context = new Context(OpService.class, SlicingService.class);
		ops = context.getService(OpService.class);
		slicerService = context.getService(SlicingService.class);
		assertTrue(ops != null);

		in = ArrayImgs.bytes(20, 20, 21);
		out = ArrayImgs.bytes(20, 20, 21);

		// fill array img with values (plane position = value in px);

		for (final Cursor<ByteType> cur = in.cursor(); cur.hasNext();) {
			cur.fwd();
			cur.get().set((byte) cur.getIntPosition(2));
		}
	}

	@Test
	public void testXYSlicing() {

		// selected interval XY
		final int[] xyAxis = new int[] { 0, 1 };

		slicerService.process(in, out, xyAxis, new DummyOp());

		for (final Cursor<ByteType> cur = out.cursor(); cur.hasNext();) {
			cur.fwd();
			assertTrue(cur.get().getRealDouble() == cur.getIntPosition(2));
		}
	}

	class DummyOp extends UnaryFunction<Iterable<ByteType>, Iterable<ByteType>> {

		@Override
		public Iterable<ByteType> compute(final Iterable<ByteType> input,
			final Iterable<ByteType> output)
		{
			final Iterator<ByteType> itA = input.iterator();
			final Iterator<ByteType> itB = output.iterator();

			while (itA.hasNext() && itB.hasNext()) {
				itB.next().set(itA.next().get());
			}
			return output;
		}

		@Override
		public UnaryFunction<Iterable<ByteType>, Iterable<ByteType>> copy() {
			return new DummyOp();
		}
	}
}
