namespace PartialNesting.Models;

internal sealed class PartialClassPart(string filePath, string typeId, string typeName)
{
    public string FilePath { get; } = filePath;
    public string TypeId { get; } = typeId;
    public string TypeName { get; } = typeName;
}
