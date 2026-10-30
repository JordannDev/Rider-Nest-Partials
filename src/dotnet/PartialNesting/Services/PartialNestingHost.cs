using JetBrains.Application.Parts;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.Interfaces;
using JetBrains.Rd.Tasks;
using JetBrains.ReSharper.Feature.Services.Protocol;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Tree;
using PartialNesting.Protocol;

namespace PartialNesting.Services;

[SolutionComponent(Instantiation.ContainerAsyncPrimaryThread)]
public sealed class PartialNestingHost
{
    /// <summary>Connects semantic discovery to the Rider frontend.</summary>
    public PartialNestingHost(Lifetime lifetime, ISolution solution, IPsiServices psiServices, ProjectContext projectContext)
    {
        var protocol = solution.GetProtocolSolution().TryGetProto()!;
        var model = new PartialNestingModel(lifetime, protocol);
        var discovery = new PartialClassDiscovery(solution, psiServices);

        model.GetGroups.SetAsync((requestLifetime, request) =>
            psiServices.Files.CommitWithRetryBackgroundRead(requestLifetime,
                () => discovery.GetGroups(requestLifetime, request)));

        void OnPsiChanged(ITreeNode? node, PsiChangedElementType changeType)
        {
            var path = node?.GetSourceFile()?.GetLocation().FullPath ?? "";

            protocol.Scheduler.Queue(() =>
            {
                if (lifetime.IsAlive)
                    model.Invalidated.Fire(path);
            });
        }

        psiServices.Files.AfterPsiChanged += OnPsiChanged;
        lifetime.OnTermination(() => psiServices.Files.AfterPsiChanged -= OnPsiChanged);

        projectContext.ContextChanged.Advise(lifetime, _ =>
            protocol.Scheduler.Queue(() =>
            {
                if (lifetime.IsAlive)
                    model.Invalidated.Fire("");
            }));
    }
}
