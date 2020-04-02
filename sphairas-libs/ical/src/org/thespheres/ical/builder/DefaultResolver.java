/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

import org.openide.util.NbBundle;
import org.thespheres.ical.CalendarComponent;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ICalendarBuilder.DefaultResolver.exceptionMessage=Found {0} CalendarComponents found with unique UID and RECURRENCE_ID"})
public class DefaultResolver implements ICalendarBuilder.Resolver {

    public enum ResolverType {

        RETURN_FIRST,
        ILLEGAL_ARGUMENT_EXCEPTION

    }
    private final ResolverType type;

    public DefaultResolver(ResolverType type) {
        this.type = type;
    }

    @Override
    public CalendarComponent resolve(CalendarComponent[] input) {
        switch (type) {
            case RETURN_FIRST:
                return input[0];
            case ILLEGAL_ARGUMENT_EXCEPTION:
                if (input.length == 1) {
                    return input[0];
                } else {
                    throw new IllegalArgumentException(NbBundle.getMessage(DefaultResolver.class, "ICalendarBuilder.DefaultResolver.exceptionMessage", input.length));
                }
        }
        throw new RuntimeException();//Cannot happen
    }

}
