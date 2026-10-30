<img src="assets/icon.svg" width="64" height="64" alt="">

# Partial Class Nesting

Nests C# files that declare the same partial class in JetBrains Rider's Solution Explorer, regardless of their filenames.

For example, `CustomerRepository.Read.cs` and `Queries.cs` appear under `CustomerRepository.cs` when they declare the same partial class.

## Behavior

- Groups files within the same project and logical folder.
- Prefers `ClassName.cs` as the parent; otherwise uses the first path in ordinal order.
- Preserves existing Designer and `DependentUpon` nesting.
- Leaves files containing unrelated partial classes separate.
- Uses the active compilation context, including namespaces, generic arity, and containing types.
- Updates after code edits, file changes, project reloads, and compilation-context changes.
- Does not modify source files or project metadata.

Supports C# partial classes. Other partial type kinds, cross-folder grouping, and virtual source-generator files are outside the current scope.

## Install

Build the plugin, then choose **Settings → Plugins → Install Plugin from Disk** and select `build/distributions/rider-partial-nesting-0.1.0.zip`. Restart Rider.

Toggle **Settings → Appearance & Behavior → Partial Class Nesting** to enable or disable it. Rider's **File Nesting Settings** switch must also be enabled.

The initial target is Rider **2026.2.3.1** (build **262.10968.170**). Compatibility is limited to the 262 release line; later versions require separate verification. Remote Development is not currently supported.

Validated locally on macOS with Apple Silicon. Intel Macs, Windows, and Linux have not been tested.

## Build and test

Requires JDK 21 and .NET SDK 10. The Gradle wrapper downloads its dependencies and the pinned Rider SDK.

```sh
./gradlew check buildPlugin verifyPlugin
```

To reuse a local Rider installation on macOS:

```sh
./gradlew check buildPlugin verifyPlugin -PriderHome="$HOME/Applications/Rider.app/Contents"
./gradlew runIde -PriderHome="$HOME/Applications/Rider.app/Contents"
```

`runIde` opens the included sample solution in an isolated sandbox with separate settings, plugins, caches, and logs. Pass `-PsandboxSolution=/absolute/path/to/Your.sln` to use another solution.

The policy tests can also run independently:

```sh
dotnet test src/dotnet/PartialNesting.Tests/PartialNesting.Tests.csproj
```

## Source layout

- `src/dotnet/PartialNesting`: ReSharper semantic discovery and grouping policy.
- `src/rider/main`: native Solution Explorer integration and settings.
- `protocol`: RD contracts; C# and Kotlin bindings are generated during the build.
- `samples/PartialClasses`: manual integration cases.

The plugin uses Rider's `solutionExplorerCustomization` extension. Files retain their native project-item identities so navigation and file actions continue to target the original items.

The backend DLL runs inside Rider's .NET host on macOS. Its `net472` build target follows the SDK template.

Rider and ReSharper dependencies must stay aligned when upgrading `riderVersion` in `gradle.properties` and `JetBrains.Rider.SDK` in the backend project.

## Development references

- [Rider plugin development](https://plugins.jetbrains.com/docs/intellij/rider.html)
- [Official plugin template](https://github.com/JetBrains/resharper-rider-plugin)
- [ReSharper declared elements](https://www.jetbrains.com/help/resharper/sdk/DeclaredElements.html)
- [Rider extension points](https://plugins.jetbrains.com/docs/intellij/rider-extension-point-list.html)
