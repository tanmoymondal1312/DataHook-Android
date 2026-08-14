# Branding assets

`logo-source.webp` (1254×1254, RGBA) is the master logo. Every icon in
`app/src/main/res` is derived from it — do not hand-edit the generated files,
regenerate them instead.

| Generated | From the source | Notes |
|---|---|---|
| `drawable-nodpi/dh_logo.webp` | scaled to 512px | in-app logo (Auth + Home). 512 is ample for the 96dp it renders at and keeps the decoded bitmap ~1 MB. |
| `mipmap-*/ic_launcher_foreground.webp` | 66% of canvas | adaptive icon; the outer third is masked away, so the art must stay in the safe zone. |
| `mipmap-*/ic_launcher.webp`, `ic_launcher_round.webp` | on white | legacy launcher icons for pre-API-26. |
| `drawable-*/ic_notification.webp` | alpha → white | notification small icon. **Android draws only the alpha channel, flat white** — colour is impossible here, which is also why an uploaded endpoint logo can only ever be the notification's *large* icon. |

Regenerate with `python branding/generate_icons.py` from the Android project root.
