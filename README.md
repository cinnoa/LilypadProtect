# LilyPadBoatGuard

Tiny PaperMC 1.20.4 plugin that stops boats from breaking lily pads when colliding with them.

## Build locally

```bash
mvn clean package
```

The jar will be in:

```text
target/LilyPadBoatGuard-1.0.0.jar
```

## Build in GitHub

Upload this whole folder to a GitHub repository. The included workflow at `.github/workflows/build.yml` will compile the jar automatically.

Go to **Actions** → **Build LilyPadBoatGuard** → run/open latest workflow → download the jar artifact.

## Command

```text
/lilypadboatguard reload
/lpbg reload
/lilypadguard reload
```

Permission:

```text
lilypadboatguard.admin
```

## Config

See `src/main/resources/config.yml`.
