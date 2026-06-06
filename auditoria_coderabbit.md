────────────────────────────────────────
CodeRabbit Review

Diff      : all local changes (committed + uncommitted)
Compare   : main → main
Directory : nerd-coffee
────────────────────────────────────────

(\(\
(• .•)  In Vino Veritas, In Codice Bugas. In wine, there is truth; in code, bugs.


────────────────────────────────────────────────────────────────────────
  major [potential_issue]
  → ]8;;vscode://file//home/punk/projetos/nerd-coffee/src/main/java/com/nerdcoffe/service/AuthService.java:59src/main/java/com/nerdcoffe/service/AuthService.java:59-62]8;;

  Auto-generated username collision should retry, not fail registration.

  The username is system-generated, not user-chosen. Throwing
  ConflictException blocks the user from registering due to an internal
  collision they cannot control. Additionally, there's a TOCTOU race between
  the existence check and save.

  Use a retry loop instead:




  🔧 Proposed fix

  -        String generatedUsername = baseUsername + "_" + java.util.UUID.randomUUID().toString().substring(0, 4);
  -
  -        if (userRepository.existsByUsername(generatedUsername)) {
  -            log.warn("Username já existe: {}", generatedUsername);
  -            throw new ConflictException("Username já existente");
  -        }
  +        String generatedUsername;
  +        int attempts = 0;
  +        do {
  +            generatedUsername = baseUsername + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
  +            attempts++;
  +            if (attempts > 5) {
  +                log.error("Failed to generate unique username after {} attempts", attempts);
  +                throw new BusinessException("Não foi possível gerar um nome de usuário único");
  +            }
  +        } while (userRepository.existsByUsername(generatedUsername));


────────────────────────────────────────
Review complete
1 finding ✔

Major    1
────────────────────────────────────────

Print all AI prompts: coderabbit review --show-prompts
