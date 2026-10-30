# Sample solution

Open `PartialClasses.csproj` in the Rider sandbox.

- `Named`: `CustomerRepository.Read.cs` and `Queries.cs` nest under `CustomerRepository.cs`. Its existing Designer and Metadata children stay directly beneath it.
- `UnrelatedNames`: `Writes.cs` nests under `Reads.cs`.
- `Namespaces`: both files stay separate.
- `Ambiguous`: all three files stay separate.
- `Nested`: `Details.cs` nests under `Container.cs`.

Change a part's namespace, rename a file, or toggle **Partial Class Nesting** in settings to check updates.
