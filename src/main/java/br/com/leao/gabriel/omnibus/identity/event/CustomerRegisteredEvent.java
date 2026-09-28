package br.com.leao.gabriel.omnibus.identity.event;

/**
 * Published when a customer has activated their account and should receive a welcome
 * confirmation.
 *
 * @param email the customer's email address
 * @param name  the customer's display name
 */
public record CustomerRegisteredEvent(String email, String name) {}
