<script>
    import { enhance } from "$app/forms";

    let { data, form } = $props();
    let flaggedContent = $state(data.flaggedContent || []);

    $effect(() => {
        flaggedContent = data.flaggedContent || [];
    });

    function formatDate(dateString) {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString("de-CH");
    }
</script>

<div class="container mt-4">
    <h2><i class="bi bi-shield-exclamation me-2"></i>Content Moderation</h2>
    <p class="text-muted">Von der KI gemeldete Inhalte</p>

    {#if form?.success && form?.action === "deleted"}
        <div class="alert alert-success">Content wurde gelöscht.</div>
    {/if}

    {#if form?.success && form?.action === "dismissed"}
        <div class="alert alert-info">Flag wurde entfernt.</div>
    {/if}

    {#if form?.error}
        <div class="alert alert-danger">{form.error}</div>
    {/if}

    {#if flaggedContent.length === 0}
        <div class="alert alert-success">
            <i class="bi bi-check-circle me-2"></i>
            Keine gemeldeten Inhalte. Alles in Ordnung!
        </div>
    {:else}
        <div class="table-responsive">
            <table class="table table-hover">
                <thead class="table-dark">
                    <tr>
                        <th>Typ</th>
                        <th>Inhalt</th>
                        <th>KI-Begründung</th>
                        <th>User</th>
                        <th>Datum</th>
                        <th>Aktionen</th>
                    </tr>
                </thead>
                <tbody>
                    {#each flaggedContent as item}
                        <tr>
                            <td>
                                <span class="badge bg-warning">{item.contentType}</span>
                            </td>
                            <td style="max-width: 250px;">
                                <small class="text-break">"{item.content}"</small>
                            </td>
                            <td style="max-width: 200px;">
                                <small class="text-muted">{item.reason}</small>
                            </td>
                            <td>
                                <small>{item.userId}</small>
                            </td>
                            <td>
                                <small>{formatDate(item.createdAt)}</small>
                            </td>
                            <td>
                                <div class="btn-group btn-group-sm">
                                    <form method="POST" action="?/deleteContent" use:enhance>
                                        <input type="hidden" name="flaggedId" value={item.id} />
                                        <input type="hidden" name="contentType" value={item.contentType} />
                                        <input type="hidden" name="contentId" value={item.contentId} />
                                        <button
                                            type="submit"
                                            class="btn btn-danger btn-sm"
                                            onclick={(e) => {
                                                if (!confirm("Content wirklich löschen?")) {
                                                    e.preventDefault();
                                                }
                                            }}
                                        >
                                            <i class="bi bi-trash"></i> Löschen
                                        </button>
                                    </form>
                                    <form method="POST" action="?/dismissFlag" use:enhance class="ms-1">
                                        <input type="hidden" name="flaggedId" value={item.id} />
                                        <button type="submit" class="btn btn-outline-secondary btn-sm">
                                            <i class="bi bi-check"></i> OK
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
</div>