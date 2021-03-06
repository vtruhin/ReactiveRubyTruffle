package org.jruby.truffle.nodes.core.behavior;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.core.CoreClass;
import org.jruby.truffle.nodes.core.CoreMethod;
import org.jruby.truffle.nodes.core.CoreMethodArrayArgumentsNode;
import org.jruby.truffle.nodes.core.behavior.utility.BehaviorOption;
import org.jruby.truffle.nodes.core.behavior.utility.WriteValue;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.objects.ReadInstanceVariableNode;
import org.jruby.truffle.nodes.objects.SelfNode;
import org.jruby.truffle.nodes.objectstorage.WriteHeadObjectFieldNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.BehaviorObject;

/**
* Created by me on 16.03.15.
*/
@CoreClass(name = "BehaviorSource")
public class BehaviorSource {

    @CoreMethod(names = "initialize", needsBlock = false, required = 1)
    public abstract static class InitializeArity1Node extends CoreMethodArrayArgumentsNode {


        @Child
        private WriteHeadObjectFieldNode writeValue;

        public InitializeArity1Node(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            writeValue = new WriteHeadObjectFieldNode(BehaviorOption.VALUE_VAR);
        }


        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, int value) {
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, long value) {
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, double value) {
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, Object value) {
            writeValue.execute(self, value);
            return self;
        }


    }

    @CoreMethod(names = {"emit","=","<="}, needsBlock = true, required = 1)
    public abstract static class EmitNode extends CoreMethodArrayArgumentsNode {

        @Child
        private WriteValue writeValue;

        @Child CallDispatchHeadNode callPropagationSelf;
        @Child CallDispatchHeadNode callPropagationOtherSources;
        @Child StartPropagationNode propagationNode;


        public EmitNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            writeValue = new WriteValue();
            callPropagationSelf = DispatchHeadNodeFactory.createMethodCallOnSelf(context);
            callPropagationOtherSources = DispatchHeadNodeFactory.createMethodCall(context);
            propagationNode = new StartPropagationNode(context,sourceSection);
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, int value) {
            startPropagation(frame, self,writeValue.execute(self, value));
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, long value) {
            startPropagation(frame, self,writeValue.execute(self, value));
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, double value) {
            startPropagation(frame, self,writeValue.execute(self, value));
            return self;
        }

        @Specialization
        public BehaviorObject init(VirtualFrame frame, BehaviorObject self, Object value) {
            startPropagation(frame, self,writeValue.execute(self, value));
            return self;
        }

        private void startPropagation(VirtualFrame frame, BehaviorObject self, boolean changed){
            //self.getId()
            self.setChanged(changed);
            propagationNode.startPropagation(frame,self,BehaviorOption.createBehaviorPropagationArgs(self.getId(),self,changed));
            self.setChanged(false);
        }
    }



    public static class StartPropagationNode extends Node {

        @Child
        CallDispatchHeadNode updateNode;
        @Child
        ReadInstanceVariableNode readValue;

        public StartPropagationNode(RubyContext context, SourceSection sourceSection) {
            updateNode = DispatchHeadNodeFactory.createMethodCall(context, true);
            readValue = new ReadInstanceVariableNode(context, sourceSection, BehaviorOption.VALUE_VAR, new SelfNode(context, sourceSection), false);
        }

        public BehaviorObject startPropagation(VirtualFrame frame, BehaviorObject self, Object[] args) {
            final BehaviorObject[] sigs = self.getSignalsThatDependOnSelf();
            for (int i = 0; i < sigs.length; i++) {
                updateNode.call(frame, sigs[i], "propagation", null, args);
            }
            return self;
        }
    }



    @CoreMethod(names = "value")
    public abstract static class ValueNode extends CoreMethodArrayArgumentsNode {

        @Child
        ReadInstanceVariableNode readValue;

        public ValueNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readValue = new ReadInstanceVariableNode(context, sourceSection, BehaviorOption.VALUE_VAR, new SelfNode(context, sourceSection), false);
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        int valueInt(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            int value = readValue.executeInteger(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        long valueLong(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            long value = readValue.executeLong(frame);
            return value;
        }


        @Specialization(rewriteOn = UnexpectedResultException.class)
        double valueDouble(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            double value = readValue.executeDouble(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        Object valueObject(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            // System.out.println("now object");
            Object value = readValue.execute(frame);
            return value;
        }

        static boolean isInt(BehaviorObject s) {
            return true;
        }

        static boolean isDouble(BehaviorObject s) {
            return false;
        }

        static boolean isNotPrimitve(BehaviorObject s) {
            return !isInt(s) && !isDouble(s);
        }

    }

    @CoreMethod(names = "now")
    public abstract static class NowNode extends CoreMethodArrayArgumentsNode {

        @Child
        ReadInstanceVariableNode readValue;

        public NowNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readValue = new ReadInstanceVariableNode(context, sourceSection, BehaviorOption.VALUE_VAR, new SelfNode(context, sourceSection), false);
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        int nowInt(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            int value = readValue.executeInteger(frame);
            return value;
        }
        @Specialization(rewriteOn = UnexpectedResultException.class)
        long nowLong(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            long value = readValue.executeLong(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        double nowDouble(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            double value = readValue.executeDouble(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        Object nowObject(VirtualFrame frame, BehaviorObject obj) throws UnexpectedResultException {
            Object value = readValue.execute(frame);
            return value;
        }

        static boolean isInt(BehaviorObject s) {
            return true;
        }

        static boolean isDouble(BehaviorObject s) {
            return false;
        }

        static boolean isNotPrimitve(BehaviorObject s) {
            return !isInt(s) && !isDouble(s);
        }

    }

}
