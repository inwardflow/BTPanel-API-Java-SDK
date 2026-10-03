# SSL Test Resources

This directory is reserved for local SSL integration-test fixtures.

- Do not commit real certificates or private keys.
- The repository ignores `*.crt`, `*.key`, `*.pem`, `*.p12`, and `*.jks` files in this directory.
- Current SSL integration tests primarily rely on configured certificate metadata rather than checked-in PEM material.
