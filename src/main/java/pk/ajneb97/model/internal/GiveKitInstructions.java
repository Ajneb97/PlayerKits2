package pk.ajneb97.model.internal;

public class GiveKitInstructions {

    private final boolean fromCommand;
    private final boolean requirementsSatisfied;
    private final boolean ignorePermission;
    private final boolean ignoreRequirements;

    public GiveKitInstructions(){
        this.fromCommand = false;
        this.requirementsSatisfied = false;
        this.ignoreRequirements = false;
        this.ignorePermission = false;
    }

    public GiveKitInstructions(boolean fromCommand,boolean requirementsSatisfied,boolean ignorePermission,boolean ignoreRequirements) {
        this.fromCommand = fromCommand;
        this.requirementsSatisfied = requirementsSatisfied;
        this.ignorePermission = ignorePermission;
        this.ignoreRequirements = ignoreRequirements;
    }

    public boolean isFromCommand() {
        return fromCommand;
    }

    public boolean isRequirementsSatisfied() {
        return requirementsSatisfied;
    }

    public boolean isIgnorePermission() {
        return ignorePermission;
    }

    public boolean shouldCheckRequirements() {
        return !ignoreRequirements;
    }
}
