package org.jruby.compiler.ir;

import org.jruby.compiler.ir.operands.Label;
import org.jruby.compiler.ir.compiler_pass.CompilerPass;
import org.jruby.compiler.ir.operands.LocalVariable;
import org.jruby.parser.StaticScope;

/**
 * IRScope is the interface for all lexically scoped constructs: Script, Module,
 * Class, Method, and Closure.  It is important to understand that a single class (e.g. MyFoo)
 * may be opened lexically in several locations in source code.  Each one of these locations will
 * have their own instance of an IRScope.
 */
public interface IRScope {
    /**
     *  Record that newName is a new method name for method with oldName
     * This is for the 'alias' keyword which resolves method names in the
     * static compile/parse-time context
     */
    public void recordMethodAlias(String newName, String oldName);

    /**
     *  Unalias 'name' and return new name
     */
    public String unaliasMethodName(String name);

    /**
     *  Get the next available unique closure id for closures in this scope
     */
    public int getNextClosureId();

    /**
     */
    public StaticScope getStaticScope();

    /**
     * Get Local Variable from this scope
     */
    public LocalVariable getLocalVariable(String name, int depth);

    public String getName();

    /**
     *  Get a new label using the provided label prefix
     */
    public Label getNewLabel(String lblPrefix);

    /**
     *  Get a new label using a generic prefix
     */
    public Label getNewLabel();
}
