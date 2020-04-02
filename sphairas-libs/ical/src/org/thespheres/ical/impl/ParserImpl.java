/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.ical.builder.AbstractComponentBuilder;
import org.thespheres.ical.IComponent;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;

/**
 *
 * @author boris.heithecker
 */
@Messages("ParserImpl.FormatException.message=A calender parser exception has occurred parsing line {0} ({1})")
abstract class ParserImpl<B extends AbstractComponentBuilder> {

    public abstract List<B> parse(LineNumberReader text) throws FormatException, IOException, InvalidComponentException;

    protected abstract AbstractComponentBuilder addComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException;

    protected abstract AbstractComponentBuilder endComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException;

    protected void parseImpl(LineNumberReader reader) throws FormatException, IOException, InvalidComponentException {
        AbstractComponentBuilder current = null;
        State state = new State();

        String line;
        while ((line = reader.readLine()) != null) {
            try {
                current = parseLine(line, state, current);
            } catch (FormatException fe) {
                String msg = NbBundle.getMessage(ParserImpl.class, "ParserImpl.FormatException.message", state.index, state.line);
                Logger.getLogger(ParserImpl.class.getName()).log(Level.FINE, msg, fe);
            }
        }
    }

    protected AbstractComponentBuilder parseLine(String line, State state, AbstractComponentBuilder current) throws FormatException, InvalidComponentException {
        state.line = line;
        int len = state.line.length();

        char c = 0;
        for (state.index = 0; state.index < len; ++state.index) {
            c = line.charAt(state.index);
            if (c == ';' || c == ':') {
                break;
            }
        }
        final String name = line.substring(0, state.index);

        if (current == null) {
            if (!IComponent.BEGIN.equals(name)) {
                throw new FormatException("Expected \"BEGIN\"", state.line, state.index);
            }
        }

        final List<Parameter> params = new ArrayList<>();
        if (name != null) {
            switch (name) {
                case IComponent.BEGIN: {
                    final String componentName = extractValue(state);
                    return addComponent(current, componentName, state);
                }
                case IComponent.END: {
                    final String componentName = extractValue(state);
                    return endComponent(current, componentName, state);
                }
            }
        }

        if (c == ';') {
            Parameter parameter;
            while ((parameter = extractParameter(state)) != null) {
                params.add(parameter);
            }
        }
        final String value = extractValue(state);
        assert current != null;
        return current.addProperty(name, value, params.stream().toArray(Parameter[]::new));
    }

    protected static String extractValue(State state) throws FormatException {
        String line = state.line;
        if (state.index >= line.length() || line.charAt(state.index) != ':') {
            throw new FormatException("Expected \":\" before end of line in " + line, state.line, state.index);
        }
        String value = line.substring(state.index + 1);
        state.index = line.length() - 1;
        return value;
    }

    protected static Parameter extractParameter(State state) throws FormatException {
        final String text = state.line;
        int len = text.length();
        String name = null;
        int startIndex = -1;
        int equalIndex = -1;
        while (state.index < len) {
            final char c = text.charAt(state.index);
            switch (c) {
                case ':':
                    if (name != null) {
                        if (equalIndex == -1) {
                            throw new FormatException("Expected  \"=\" within parameter in " + text, state.line, state.index);
                        }
                        final String value = text.substring(equalIndex + 1, state.index);
                        return new Parameter(name, value);
                    }
                    //No parameter found
                    return null;
                case ';':
                    if (name != null) {
                        if (equalIndex == -1) {
                            throw new FormatException("Expected \"=\" within parameter in " + text, state.line, state.index);
                        }
                        final String value = text.substring(equalIndex + 1, state.index);
                        return new Parameter(name, value);
                    }
                    if (startIndex != -1) {
                        throw new FormatException("Expected \"=\" within parameter in " + text, state.line, state.index);
                    }
                    startIndex = state.index;
                    break;
                case '=':
                    equalIndex = state.index;
                    if (startIndex == -1) {
                        throw new FormatException("Expected \";\" before '=' in " + text, state.line, state.index);
                    }
                    name = text.substring(startIndex + 1, equalIndex);
                    break;
                case '"':
                    if (name == null) {
                        throw new FormatException("Expected parameter before \"\"\" in " + text, state.line, state.index);
                    }
                    if (equalIndex == -1) {
                        throw new FormatException("Expected \"=\" within parameter in " + text, state.line, state.index);
                    }
                    if (state.index > equalIndex + 1) {
                        throw new FormatException("Parameter value cannot contain a \"\"\" in " + text, state.line, state.index);
                    }
                    final int endQuote = text.indexOf('"', state.index + 1);
                    if (endQuote < 0) {
                        throw new FormatException("Expected closing \"\"\" in " + text, state.line, state.index);
                    }
                    final String value = text.substring(state.index + 1, endQuote);
                    state.index = endQuote + 1;
                    return new Parameter(name, value);
                default:
                    break;
            }
            ++state.index;
        }
        throw new FormatException("Expected \":\" before end of line in " + text, state.line, state.index);
    }

    protected static final class State {

        protected int index = 0;
        protected String line;

    }
}
