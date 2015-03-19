package org.jruby.truffle.nodes.core;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.literal.ObjectLiteralNode;
import org.jruby.truffle.nodes.methods.arguments.ReadAllArgumentsNode;
import org.jruby.truffle.nodes.objects.ReadInstanceVariableNode;
import org.jruby.truffle.nodes.objects.SelfNode;
import org.jruby.truffle.nodes.objects.WriteInstanceVariableNode;
import org.jruby.truffle.nodes.objectstorage.WriteHeadObjectFieldNode;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyProc;
import org.jruby.truffle.runtime.signalRuntime.SignalRuntime;
import sun.misc.Signal;

/**
 * Created by me on 16.03.15.
 */
@CoreClass(name = "BehaviorSource")
public class BehaviorSource extends BehaviourSuper{

    @CoreMethod(names = "initialize", needsBlock = true, required = 1)
    public abstract static class InitializeArity1Node extends CoreMethodNode {

        @Child
        private WriteHeadObjectFieldNode writeValue;

        public InitializeArity1Node(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            writeValue = new WriteHeadObjectFieldNode("@value");
        }

        public InitializeArity1Node(InitializeArity1Node prev) {
            super(prev);
            writeValue = prev.writeValue;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, int value) {
            self.getSources().add(self);
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, long value) {
            self.getSources().add(self);
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, double value) {
            self.getSources().add(self);
            writeValue.execute(self, value);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, Object value) {
            self.getSources().add(self);
            writeValue.execute(self, value);
            return self;
        }
    }

    @CoreMethod(names = "emit", needsBlock = true, required = 1)
    public abstract static class EmitNode extends CoreMethodNode {

        @Child
        private WriteHeadObjectFieldNode writeValue;

        @Child CallDispatchHeadNode callPropagationSelf;
        @Child CallDispatchHeadNode callPropagationOtherSources;


        public EmitNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            writeValue = new WriteHeadObjectFieldNode("@value");
            callPropagationSelf = DispatchHeadNodeFactory.createMethodCallOnSelf(context);
            callPropagationOtherSources = DispatchHeadNodeFactory.createMethodCall(context);
        }

        public EmitNode(EmitNode prev) {
            super(prev);
            writeValue = prev.writeValue;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, int value) {
            writeValue.execute(self, value);
            startPropatation(frame,self);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, long value) {
            writeValue.execute(self, value);
            startPropatation(frame,self);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, double value) {
            writeValue.execute(self, value);
            startPropatation(frame,self);
            return self;
        }

        @Specialization
        public SignalRuntime init(VirtualFrame frame, SignalRuntime self, Object value) {
            writeValue.execute(self, value);
            startPropatation(frame,self);
            return self;
        }

        private void startPropatation(VirtualFrame frame, SignalRuntime self){
            callPropagationSelf.call(frame,self,"startPropagation",null,new Object[0]);
        }
    }


    @CoreMethod(names = "startPropagation", needsSelf = true)
    public abstract static class StartPropagationNode extends CoreMethodNode {

        @Child
        CallDispatchHeadNode updateNode;
        @Child
        ReadInstanceVariableNode readValue;

        public StartPropagationNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            updateNode = DispatchHeadNodeFactory.createMethodCall(context, true);
            readValue = new ReadInstanceVariableNode(context, sourceSection, "@value", new SelfNode(context, sourceSection), false);
        }

        public StartPropagationNode(StartPropagationNode prev) {
            super(prev);
            updateNode = prev.updateNode;
            readValue = prev.readValue;
        }

        @Specialization
        public SignalRuntime startPropagation(VirtualFrame frame, SignalRuntime self) {
            final SignalRuntime[] sigs = self.getSignalsThatDependOnSelf();
            for (int i = 0; i < sigs.length; i++) {
                updateNode.call(frame, sigs[i], "propagation", null, new Object[0]);
            }
            return self;
        }
    }



    @CoreMethod(names = "value")
    public abstract static class ValueNode extends CoreMethodNode {

        @Child
        ReadInstanceVariableNode readValue;
        @Child
        ReadInstanceVariableNode readOuterSignal;

        public ValueNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readValue = new ReadInstanceVariableNode(context, sourceSection, VALUE_VAR, new SelfNode(context, sourceSection), false);
            readOuterSignal = new ReadInstanceVariableNode(context, sourceSection, "$outerSignalHack", new ObjectLiteralNode(context, sourceSection, context.getCoreLibrary().getGlobalVariablesObject()), true);
        }

        public ValueNode(ValueNode prev) {
            super(prev);
            readValue = prev.readValue;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        int valueInt(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            int value = readValue.executeIntegerFixnum(frame);
            SignalRuntime outerSignalRuntime = readOuterSignal.executeSignalRuntime(frame);
            obj.addSignalThatDependsOnSelf(outerSignalRuntime);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        long valueLong(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            long value = readValue.executeLongFixnum(frame);
            SignalRuntime outerSignalRuntime = readOuterSignal.executeSignalRuntime(frame);
            obj.addSignalThatDependsOnSelf(outerSignalRuntime);
            return value;
        }


        @Specialization(rewriteOn = UnexpectedResultException.class)
        double valueDouble(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            double value = readValue.executeFloat(frame);
            SignalRuntime outerSignalRuntime = readOuterSignal.executeSignalRuntime(frame);
            obj.addSignalThatDependsOnSelf(outerSignalRuntime);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        Object valueObject(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            // System.out.println("now object");
            Object value = readValue.execute(frame);
            return value;
        }

        static boolean isInt(SignalRuntime s) {
            return true;
        }

        static boolean isDouble(SignalRuntime s) {
            return false;
        }

        static boolean isNotPrimitve(SignalRuntime s) {
            return !isInt(s) && !isDouble(s);
        }

    }

    @CoreMethod(names = "now")
    public abstract static class NowNode extends CoreMethodNode {

        @Child
        ReadInstanceVariableNode readValue;

        public NowNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
            readValue = new ReadInstanceVariableNode(context, sourceSection, VALUE_VAR, new SelfNode(context, sourceSection), false);
        }

        public NowNode(NowNode prev) {
            super(prev);
            readValue = prev.readValue;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        int nowInt(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            int value = readValue.executeIntegerFixnum(frame);
            return value;
        }
        @Specialization(rewriteOn = UnexpectedResultException.class)
        long nowLong(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            long value = readValue.executeIntegerFixnum(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        double nowDouble(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            double value = readValue.executeFloat(frame);
            return value;
        }

        @Specialization(rewriteOn = UnexpectedResultException.class)
        Object nowObject(VirtualFrame frame, SignalRuntime obj) throws UnexpectedResultException {
            Object value = readValue.execute(frame);
            return value;
        }

        static boolean isInt(SignalRuntime s) {
            return true;
        }

        static boolean isDouble(SignalRuntime s) {
            return false;
        }

        static boolean isNotPrimitve(SignalRuntime s) {
            return !isInt(s) && !isDouble(s);
        }

    }
}
