/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops.geom.geom3d;

import java.util.Iterator;

import net.imagej.ops.Contingent;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.geom.geom3d.mesh.Facet;
import net.imagej.ops.geom.geom3d.mesh.Mesh;
import net.imagej.ops.geom.geom3d.mesh.TriangularFacet;
import net.imagej.ops.special.function.AbstractUnaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imagej.ops.special.function.UnaryFunctionOp;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.IterableRegion;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.scijava.plugin.Plugin;

/**
 * This {@link Op} computes the 2nd multi variate of a {@link IterableRegion}
 * (Label).
 * 
 * @author Tim-Oliver Buchholz (University of Konstanz)
 */
@Plugin(type = Ops.Geometric.SecondMoment.class)
public class DefaultInertiaTensor3DMesh extends AbstractUnaryFunctionOp<Mesh, RealMatrix>
		implements Ops.Geometric.SecondMoment, Contingent {

	private UnaryFunctionOp<Mesh, RealLocalizable> centroid;

	@Override
	public void initialize() {
		centroid = Functions.unary(ops(), Ops.Geometric.Centroid.class, RealLocalizable.class, in());
	}

	@Override
	public RealMatrix calculate(final Mesh input) {
		final RealLocalizable o = centroid.calculate(input);
		BlockRealMatrix tensor = new BlockRealMatrix(3, 3);

		final Iterator<Facet> c = input.getFacets().iterator();
		while (c.hasNext()) {
			final TriangularFacet tf = (TriangularFacet) c.next();
			tensor = tensor.add(tetrahedronInertiaTensor(tf.getVertex(0), tf.getVertex(1), tf.getVertex(2), o));
		}

		return tensor;
	}

	/**
	 * The computations are based on this paper:
	 * http://docsdrive.com/pdfs/sciencepublications/jmssp/2005/8-11.pdf
	 * 
	 * Note: In the paper b' and c' are swapped.
	 * 
	 * @param p1
	 *            triangular facet point
	 * @param p2
	 *            triangular facet point
	 * @param p3
	 *            triangular facet point
	 * @param cent
	 *            of the mesh
	 * @return inertia tensor of this tetrahedron
	 */
	private BlockRealMatrix tetrahedronInertiaTensor(final RealLocalizable p1, final RealLocalizable p2,
			final RealLocalizable p3, final RealLocalizable cent) {
		final double originX = cent.getDoublePosition(0);
		final double originY = cent.getDoublePosition(1);
		final double originZ = cent.getDoublePosition(2);

		final double x1 = p1.getDoublePosition(0) - originX;
		final double y1 = p1.getDoublePosition(1) - originY;
		final double z1 = p1.getDoublePosition(2) - originZ;

		final double x2 = p2.getDoublePosition(0) - originX;
		final double y2 = p2.getDoublePosition(1) - originY;
		final double z2 = p2.getDoublePosition(2) - originZ;

		final double x3 = p3.getDoublePosition(0) - originX;
		final double y3 = p3.getDoublePosition(1) - originY;
		final double z3 = p3.getDoublePosition(2) - originZ;

		final double volume = tetrahedronVolume(new Vector3D(x1, y1, z1), new Vector3D(x2, y2, z2),
				new Vector3D(x3, y3, z3));

		final double a = 6 * volume * (y1 * y1 + y1 * y2 + y2 * y2 + y1 * y3 + y2 * y3 + y3 * y3 + z1 * z1 + z1 * z2
				+ z2 * z2 + z1 * z3 + z2 * z3 + z3 * z3) / 60.0;
		final double b = 6 * volume * (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 * x3 + x3 * x3 + z1 * z1 + z1 * z2
				+ z2 * z2 + z1 * z3 + z2 * z3 + z3 * z3) / 60.0;
		final double c = 6 * volume * (x1 * x1 + x1 * x2 + x2 * x2 + x1 * x3 + x2 * x3 + x3 * x3 + y1 * y1 + y1 * y2
				+ y2 * y2 + y1 * y3 + y2 * y3 + y3 * y3) / 60.0;
		final double aa = 6 * volume
				* (2 * y1 * z1 + y2 * z1 + y3 * z1 + y1 * z2 + 2 * y2 * z2 + y3 * z2 + y1 * z3 + y2 * z3 + 2 * y3 * z3)
				/ 120.0;

		final double bb = 6 * volume
				* (2 * x1 * y1 + x2 * y1 + x3 * y1 + x1 * y2 + 2 * x2 * y2 + x3 * y2 + x1 * y3 + x2 * y3 + 2 * x3 * y3)
				/ 120.0;

		final double cc = 6 * volume
				* (2 * x1 * z1 + x2 * z1 + x3 * z1 + x1 * z2 + 2 * x2 * z2 + x3 * z2 + x1 * z3 + x2 * z3 + 2 * x3 * z3)
				/ 120.0;

		final BlockRealMatrix t = new BlockRealMatrix(3, 3);
		t.setRow(0, new double[] { a, -bb, -cc });
		t.setRow(1, new double[] { -bb, b, -aa });
		t.setRow(2, new double[] { -cc, -aa, c });

		return t;
	}

	private double tetrahedronVolume(Vector3D a, Vector3D b, Vector3D c) {
		// https://en.wikipedia.org/wiki/Tetrahedron#Volume
		return Math.abs(a.dotProduct(b.crossProduct(c))) / 6.0;
	}

	@Override
	public boolean conforms() {
		return in() != null;
	}

}
