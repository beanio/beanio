/*
 * Copyright 2011-2012 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.internal.parser;

import java.util.ArrayList;

/**
 * Base class for the parsing context- marshalling or unmarshaling.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public abstract class ParsingContext {

    /** Unmarshalling mode */
    public static final char UNMARSHALLING = 'U';
    /** Marsalling mode */
    public static final char MARSHALLING = 'M';
    
    private ArrayList<Iteration> iterationStack = new ArrayList<Iteration>();
    
    /**
     * Constructs a new <tt>ParsingContext</tt>.
     */
    public ParsingContext() { }
    
    /**
     * Returns the parsing mode.
     * @return either {@link #UNMARSHALLING} or {@link #MARSHALLING}
     */
    public abstract char getMode();
    
    /**
     * Pushes an {@link Iteration} onto a stack for adjusting
     * field positions and indices.  
     * @param iteration the {@link Iteration} to push
     * @see #popIteration()
     */
    public void pushIteration(Iteration iteration) {
        iterationStack.add(iteration);
    }
    
    /**
     * Pops the last {@link Iteration} pushed onto the stack.
     * @see #pushIteration(Iteration)
     */
    public Iteration popIteration() {
        return iterationStack.remove(iterationStack.size() - 1);
    }
    
    /**
     * Calculates a field position by adjusting for any applied iterations.
     * @param position the field position to adjust (i.e. the position of the first
     *   occurrence of the field)
     * @return the adjusted field position
     */
    public final int getAdjustedFieldPosition(int position) {
        for (Iteration i : iterationStack) {
            position += i.getIterationIndex() * i.getIterationSize();
        }
        return position;
    }
    
    /**
     * Returns the current field index adjusting for any applied iterations.
     * @return the current field index
     */
    public final int getAdjustedFieldIndex() {
        if (iterationStack.isEmpty()) {
            return 0;
        }
        else {
            return iterationStack.get(iterationStack.size() - 1).getIterationIndex();
        }
    }
}
