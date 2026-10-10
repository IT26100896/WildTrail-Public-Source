package com.wildtrail.security;

public record SessionUser(int uid, String email, String name, String role) {
}
