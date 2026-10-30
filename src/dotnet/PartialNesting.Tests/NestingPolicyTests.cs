using PartialNesting.Models;
using PartialNesting.Services;
using Xunit;

namespace PartialNesting.Tests;

public sealed class NestingPolicyTests
{
    [Fact]
    public void PrefersTheFileNamedAfterTheClass()
    {
        var groups = NestingPolicy.Group([
            new("/src/Queries.cs", "project:CustomerRepository", "CustomerRepository"),
            new("/src/CustomerRepository.cs", "project:CustomerRepository", "CustomerRepository")
        ]);

        var group = Assert.Single(groups);
        Assert.Equal("/src/CustomerRepository.cs", group.ParentPath);
        Assert.Equal(["/src/Queries.cs"], group.ChildPaths);
    }

    [Fact]
    public void GroupsPartsWithoutMatchingFileNames()
    {
        var group = Assert.Single(NestingPolicy.Group([
            new("/src/Writes.cs", "project:Repository", "Repository"),
            new("/src/Reads.cs", "project:Repository", "Repository")
        ]));

        Assert.Equal("/src/Reads.cs", group.ParentPath);
        Assert.Equal(["/src/Writes.cs"], group.ChildPaths);
    }

    [Fact]
    public void ParentSelectionDoesNotDependOnDiscoveryOrder()
    {
        PartialClassPart[] parts = [
            new("/src/B.cs", "project:Repository", "Repository"),
            new("/src/A.cs", "project:Repository", "Repository"),
            new("/src/C.cs", "project:Repository", "Repository")
        ];

        var first = Assert.Single(NestingPolicy.Group(parts));
        var second = Assert.Single(NestingPolicy.Group(parts.Reverse()));

        Assert.Equal(first.ParentPath, second.ParentPath);
        Assert.Equal(first.ChildPaths, second.ChildPaths);
    }

    [Theory]
    [InlineData("project:Sales.Customer", "project:Support.Customer")]
    [InlineData("project-a:Customer", "project-b:Customer")]
    [InlineData("project:Customer`1", "project:Customer`2")]
    [InlineData("project:OuterA+Customer", "project:OuterB+Customer")]
    public void KeepsDifferentTypeIdentitiesSeparate(string firstType, string secondType)
    {
        Assert.Empty(NestingPolicy.Group([
            new("/src/First.cs", firstType, "Customer"),
            new("/src/Second.cs", secondType, "Customer")
        ]));
    }

    [Fact]
    public void LeavesFilesWithUnrelatedPartialClassesUnnested()
    {
        var groups = NestingPolicy.Group([
            new("/src/Shared.cs", "project:First", "First"),
            new("/src/Shared.cs", "project:Second", "Second"),
            new("/src/First.cs", "project:First", "First"),
            new("/src/Second.cs", "project:Second", "Second")
        ]);

        Assert.Empty(groups);
    }

    [Fact]
    public void RepeatedDeclarationsInOneFileDoNotCreateChildren()
    {
        Assert.Empty(NestingPolicy.Group([
            new("/src/Customer.cs", "project:Customer", "Customer"),
            new("/src/Customer.cs", "project:Customer", "Customer")
        ]));
    }

    [Fact]
    public void PreservesCaseDistinctFilePaths()
    {
        var group = Assert.Single(NestingPolicy.Group([
            new("/src/Customer.cs", "project:Customer", "Customer"),
            new("/src/customer.cs", "project:Customer", "Customer")
        ]));

        Assert.Equal("/src/Customer.cs", group.ParentPath);
        Assert.Equal(["/src/customer.cs"], group.ChildPaths);
    }
}
