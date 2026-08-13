package com.construction.material.repository;

public interface AlertRepositoryExtensions {
    long countByActiveTrueAndAcknowledgedFalse();
}
