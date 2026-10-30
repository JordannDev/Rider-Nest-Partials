using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using PartialNesting.Models;

namespace PartialNesting.Services;

internal static class NestingPolicy
{
    /// <summary>Groups unambiguous parts and chooses a stable parent file.</summary>
    public static IReadOnlyList<FileGroup> Group(IEnumerable<PartialClassPart> parts)
    {
        var eligibleParts = parts
            .GroupBy(part => part.FilePath, StringComparer.Ordinal)
            .Where(file => file.Select(part => part.TypeId).Distinct(StringComparer.Ordinal).Count() == 1)
            .Select(file => file.First());

        return eligibleParts
            .GroupBy(part => part.TypeId, StringComparer.Ordinal)
            .Select(group => group
                .OrderBy(part => Path.GetFileName(part.FilePath) == part.TypeName + ".cs" ? 0 : 1)
                .ThenBy(part => part.FilePath, StringComparer.Ordinal)
                .ToArray())
            .Where(group => group.Length > 1)
            .Select(group => new FileGroup(group[0].FilePath, group.Skip(1).Select(part => part.FilePath).ToArray()))
            .ToArray();
    }
}
