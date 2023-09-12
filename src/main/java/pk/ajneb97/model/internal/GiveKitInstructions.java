package pk.ajneb97.model.internal;

public class GiveKitInstructions {

    private boolean fromCommand; //Allows to ignore some errors, like permissions, cooldown, one time...
    private boolean requirementsSatisfied; //The player is buying the kit and all requirements are satisfied
    private boolean ignorePermission;
    private boolean ignoreRequirements;

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

    public void setFromCommand(boolean fromCommand) {
        this.fromCommand = fromCommand;
    }

    public boolean isRequirementsSatisfied() {
        return requirementsSatisfied;
    }

    public void setRequirementsSatisfied(boolean requirementsSatisfied) {
        this.requirementsSatisfied = requirementsSatisfied;
    }

    public boolean isIgnorePermission() {
        return ignorePermission;
    }

    public void setIgnorePermission(boolean ignorePermission) {
        this.ignorePermission = ignorePermission;
    }

    public boolean isIgnoreRequirements() {
        return ignoreRequirements;
    }

    public void setIgnoreRequirements(boolean ignoreRequirements) {
        this.ignoreRequirements = ignoreRequirements;
    }
}
