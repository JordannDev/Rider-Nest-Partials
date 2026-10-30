using System.Collections.Generic;

namespace PartialNesting.Models;

internal sealed class FileGroup(string parentPath, IReadOnlyList<string> childPaths)
{
    public string ParentPath { get; } = parentPath;
    public IReadOnlyList<string> ChildPaths { get; } = childPaths;
}
