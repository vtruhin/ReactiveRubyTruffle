package org.jruby.truffle.nodes.core.behavior.propagation;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.core.behavior.propagation.HandleOnChangeNodeGen;
import org.jruby.truffle.nodes.core.behavior.functionality.HandleBehaviorFunctionality;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.BehaviorObject;


public class PropagationMainMethodNode extends Node {
    @Child
    protected
    NotifySuccessorNode handlePropagation;
    @Child
    protected
    GlitchFreedomCheckNode propagationNode;
    @Child
    protected
    HandleBehaviorFunctionality handleBehaviorExpr;
    @Child
    protected
    HandleOnChange handleOnChange;

    public PropagationMainMethodNode(RubyContext context, SourceSection section) {
        super(section);
        propagationNode = GlitchFreedomCheckNode.createUninitializedShouldPropagationNode(context, section);
        handleBehaviorExpr = HandleBehaviorFunctionality.createHandleBehaviorExprNode(context, section);
        handlePropagation = new NotifySuccessorNode(context, section);
        handleOnChange = HandleOnChangeNodeGen.create(context);
    }


    public void execute(VirtualFrame frame, BehaviorObject self, long sourceId, boolean lastNodeChanged, BehaviorObject lastNode) {
        self.setChanged(self.isChanged() || lastNodeChanged);
        if (propagationNode.canContinuePropagation(frame, self, sourceId)) {
            if (self.isChanged()) {
                boolean changed = handleBehaviorExpr.execute(frame, self, lastNode,sourceId);
                self.setChanged(changed);
                if(changed)
                    handleOnChange.execute(frame, self);
                handlePropagation.execute(frame, self, sourceId, changed);
            } else {
                handlePropagation.execute(frame, self, sourceId, false);
            }
            self.setChanged(false);
            self.setCount(0);
        }
    }
}







