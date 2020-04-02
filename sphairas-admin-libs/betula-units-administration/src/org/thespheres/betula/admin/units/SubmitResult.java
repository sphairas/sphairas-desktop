/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

/**
 *
 * @author boris.heithecker
 */
public enum SubmitResult {
    OK,
    EXCEPTION;

    public static boolean isOK(final SubmitResult res) {
        return res != null && res == OK;
    }
}
