/*
 *  Copyright 2013 Christoph Böhme
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.b3e.mf.extra.source;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walks through a file tree tree and emits the name of 
 * each file found.
 * 
 * @author Christoph Böhme
 * 
 */
@Description("Walks through a file tree and emits the name of each file found.")
@In(String.class)
@Out(String.class)
public final class FileTreeWalker extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	private static final Logger LOG = LoggerFactory.getLogger(FileTreeWalker.class);
	
	private final Visitor visitor = new Visitor();
	private final Set<FileVisitOption> visitOptions = EnumSet.noneOf(FileVisitOption.class);
	
	private int maxDepth = Integer.MAX_VALUE;
	
	/**
	 * Returns whether symbolic links are followed.
	 * 
	 * @return true if symbolic links are followed
	 */
	public boolean isFollowingLinks() {
		return visitOptions.contains(FileVisitOption.FOLLOW_LINKS);
	}
	
	/**
	 * Configures whether to follow symbolic links or not
	 * 
	 * @param follow if true symbolic links are followed
	 */
	public void setFollowLinks(final boolean follow) {
		if (follow) {
			visitOptions.add(FileVisitOption.FOLLOW_LINKS);
		} else {
			visitOptions.remove(FileVisitOption.FOLLOW_LINKS);
		}
	}
	
	/**
	 * Returns the maximum depth to which the walker will descend 
	 * in the directory hierarchy.
	 * 
	 * @return max visitation depth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}
	
	/**
	 * Sets the maximum depth to which the walker should descend
	 * in the directory hierarchy.
	 * 
	 * @param maxDepth sets the visitation depth. 0 means only
	 *                 visiting the start node. Integer.MAX_VALUE
	 *                 means descend as deep as possible.
	 */
	public void setMaxDepth(final int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	@Override
	public void process(final String directory) {
		try {
			Files.walkFileTree(Paths.get(directory), visitOptions, maxDepth, visitor);
		} catch (IOException e) {
			throw new MetafactureException(e);
		}
	}

	/**
	 * Visitor implementation
	 */
	private class Visitor extends SimpleFileVisitor<Path> {

		@Override
		public FileVisitResult visitFile(final Path file,
				final BasicFileAttributes attrs) {
			if (attrs.isRegularFile()) {
				getReceiver().process(file.toAbsolutePath().toString());
			}
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
			LOG.warn("Failed visiting directory/file '{}': {}", file.toAbsolutePath().toString(), exc.toString());
			return FileVisitResult.CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
			if (exc != null) {
				LOG.warn("Aborted directory visit '{}': {}", dir.toAbsolutePath().toString(), exc.toString());						
			}
			return FileVisitResult.CONTINUE;
			
		}

	}
	
}
