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

package imagej.ops;

import imagej.command.CommandInfo;
import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.service.ImageJService;

import java.util.List;

import org.scijava.plugin.SingletonService;

/**
 * Interface for services that find {@link Op}s which match a template.
 * 
 * @author Curtis Rueden
 */
public interface OpMatcherService extends SingletonService<OpMatcher>,
	ImageJService
{

	/** Gets the list of all available {@link Op} implementations. */
	public List<CommandInfo> getOps();

	/**
	 * Finds and initializes the best module matching the given op name and/or
	 * type + arguments.
	 * 
	 * @param name The op's name, or null to match all names.
	 * @param type Required type of the op, or null to match all types.
	 * @param args The op's input arguments.
	 * @throws IllegalArgumentException if there is no match, or if there is more
	 *           than one match at the same priority.
	 */
	public Module findModule(String name, Class<? extends Op> type,
		Object... args);

	/**
	 * Builds a list of candidate ops which match the given name and class.
	 * 
	 * @param name The op's name, or null to match all names.
	 * @param type Required type of the op, or null to match all types.
	 * @return The list of candidates as {@link ModuleInfo} metadata.
	 */
	List<ModuleInfo> findCandidates(String name, Class<? extends Op> type);

	/**
	 * Filters a list of ops to those matching the given arguments.
	 * 
	 * @param ops The list of ops to scan for matches.
	 * @param args The op's input arguments.
	 * @return The list of matching ops as {@link Module} instances.
	 */
	List<Module> findMatches(List<? extends ModuleInfo> ops, Object... args);

	/** Assigns arguments into the given module's inputs. */
	Module assignInputs(Module module, Object... args);

	/**
	 * Gets a string describing the given op template.
	 * 
	 * @param name The op's name.
	 * @param args The op's input arguments.
	 * @return A string describing the op template.
	 */
	String getOpString(String name, Object... args);

	/**
	 * Gets a string describing the given op.
	 * 
	 * @param info The {@link ModuleInfo} metadata which describes the op.
	 * @return A string describing the op.
	 */
	String getOpString(ModuleInfo info);

	boolean isCandidate(CommandInfo info, String name);

	boolean isCandidate(CommandInfo info, Class<? extends Op> type);

	boolean isCandidate(CommandInfo info, String name, Class<? extends Op> type);

}
