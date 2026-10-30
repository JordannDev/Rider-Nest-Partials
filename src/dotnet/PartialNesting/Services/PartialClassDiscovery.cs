using System;
using System.Collections.Generic;
using System.Linq;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.ReSharper.Psi.CSharp.Tree;
using JetBrains.ReSharper.Psi.Files;
using JetBrains.ReSharper.Psi.Tree;
using JetBrains.Util;
using PartialNesting.Models;
using PartialNesting.Protocol;

namespace PartialNesting.Services;

internal sealed class PartialClassDiscovery(ISolution solution, IPsiServices psiServices)
{
    /// <summary>Reads partial classes from the requested project's visible files.</summary>
    public List<NestingGroup> GetGroups(Lifetime lifetime, FolderRequest request)
    {
        var parts = new List<PartialClassPart>();
        var projectLocation = VirtualFileSystemPath.Parse(request.ProjectPath, InteractionContext.SolutionContext);

        foreach (var path in request.FilePaths.Distinct(StringComparer.Ordinal))
        {
            lifetime.ThrowIfNotAlive();
            var location = VirtualFileSystemPath.Parse(path, InteractionContext.SolutionContext);
            var projectFile = solution.FindProjectItemsByLocation(location)
                .OfType<IProjectFile>()
                .FirstOrDefault(file => file.GetProject().ProjectFileLocation.Equals(projectLocation));

            if (projectFile == null)
                continue;

            foreach (var sourceFile in projectFile.ToSourceFilesInCurrentContext())
            {
                foreach (var file in psiServices.Files.GetPsiFiles<CSharpLanguage>(sourceFile, PsiLanguageCategories.Primary))
                {
                    foreach (var declaration in file.Descendants<IClassDeclaration>())
                    {
                        if (!declaration.IsPartial || HasPartialClassParent(declaration) || declaration.DeclaredElement is not { } type)
                            continue;

                        var typeId = sourceFile.PsiModule.GetPersistentID() + ":" + type.GetClrName().FullName;
                        parts.Add(new PartialClassPart(path, typeId, type.ShortName));
                    }
                }
            }
        }

        return NestingPolicy.Group(parts)
            .Select(group => new NestingGroup(group.ParentPath, group.ChildPaths.ToList()))
            .ToList();
    }

    /// <summary>Lets the enclosing partial class own nested declarations.</summary>
    private static bool HasPartialClassParent(IClassDeclaration declaration)
    {
        for (var parent = declaration.Parent; parent != null; parent = parent.Parent)
        {
            if (parent is IClassDeclaration { IsPartial: true })
                return true;
        }

        return false;
    }
}
