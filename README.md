# LUI

Typed Laminar UI components for Scala.js. Zero CSS files, zero npm. See `NEXT.md` for the full design plan.

## Development

Two terminals:

```
sbt ~fastLinkJS       # incremental Scala.js compile → example/public/scripts/
sbt devserver/run     # serves example/public/ on http://localhost:8080
```

Refresh the browser after edits.
