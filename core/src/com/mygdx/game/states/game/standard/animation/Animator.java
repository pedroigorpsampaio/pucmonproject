package com.mygdx.game.states.game.standard.animation;

/**
 * State machine to control animation states
 * to be incorporated by dynamic animated entities
 *
 * @author Pedro Sampaio
 * @since  0.6
 */
public class Animator {

    private Class<? extends Enum<?>> animStates;    // the states of the state machine
    private Class<? extends Enum<?>> animCommands;  // the possible commands for the state machine transitions

    private Enum<?> currentState;   // the current state of the animator
    java.util.Map<StateTransition, Enum<?>> transitions; // possible transitions of state machine
    java.util.Map<Enum<?>, Animation> animations; // the animations of each state

    /**
     * Constructor
     * @param animStates    the states of the state machine
     * @param animCommands  the possible commands for the state machine transitions
     * @param currentState  the current state of animator state machine (initial state)
     * @param transitions   the possible transitions in the animator state machine
     * @param animations    the animations of each state
     * @param <E>   enum for animation states class
     * @param <E2>  enum for animation commands class
     */
    public <E extends Enum<E>, E2 extends Enum<E2>> Animator(Class<E> animStates,
                                                             Class<E2> animCommands,
                                                             Enum<E> currentState,
                                                             java.util.Map<StateTransition, Enum<?>> transitions,
                                                             java.util.Map<Enum<?>, Animation> animations) {
        this.animStates = animStates;
        this.animCommands = animCommands;
        this.currentState = currentState;
        this.transitions = transitions;
        this.animations = animations;
    }

    /**
     * Updates current state animation
     */
    public void update() {
        // gets animation from current state
        Animation currAnim = animations.get(currentState);

        // if animations is finished (in case of non-loopable animations)
        // goes to the exit animation linked
        if(currAnim.isFinished()) {
            for (Enum<? extends Enum> e : animCommands.getEnumConstants()) {
                if(e.name().equals("exit")) // found exit command to transition animator
                    getNext(e); // makes exit transition in animator
            }
        }

        // if found an animation, updates it
        if(currAnim != null)
            currAnim.update();
    }

    /**
     * Represents a state transition
     *
     * @author Pedro Sampaio
     * @since 0.6
     */
    public static class StateTransition
    {
        private Enum<?> currentState;
        private Enum<?> command;

        /**
         * Constructor
         * @param currentState  current state to create transition
         * @param command       command to trigger transition
         * @param <E>           animator state
         * @param <E2>          animator command
         */
        public <E extends Enum<E>, E2 extends Enum<E2>> StateTransition(Enum<E> currentState, Enum<E2> command)
        {
            this.currentState = currentState;
            this.command = command;
        }
    }

    /**
     * Returns the next state of state machine
     * when triggered received command
     *
     * @param command the command to trigger in state machine
     * @param <E2> the animator command class
     * @return  the new state after triggering the command received
     *          or current state if command has not changed states
     */
    public <E2 extends Enum<E2>> Enum<?> getNext(Enum<E2> command)
    {
        // prepares transition
        StateTransition transition = new StateTransition(currentState, command);
        // tries to find transition
        Enum<?> nextState = findNext(transition);
        if (nextState == null) { // if not found, return the same state
            return currentState;
        }
        // resets old state animation
        animations.get(currentState).resetAnimation();
        // sets new current state
        currentState = nextState;
        // returns new state
        return nextState;
    }

    /**
     * Searches for transition in transitions hash map
     * returning the next state of transition
     * @param transition the transition to be searched
     * @return the next state of transition or null if transition was not found
     */
    private Enum<?> findNext(StateTransition transition) {
        // iterates through transitions map
        for (java.util.Map.Entry<StateTransition, Enum<?>> entry : transitions.entrySet()) {
            if(entry.getKey().currentState.equals(transition.currentState) &&
                    entry.getKey().command.equals(transition.command))
                return entry.getValue();
        }
        return null; // transition not found
    }

    /**
     * returns the current state of animator state machine
     * @return the current state of animator state machine
     */
    public Enum<?> getCurrentState() {
        return currentState;
    }

    /**
     * returns the current animation based on current state
     * @return the current animation to be rendered
     */
    public Animation getCurrentAnimation() {
        return animations.get(currentState);
    }
}
