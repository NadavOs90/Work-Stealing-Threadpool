package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.tools.Tool;

/**
 * Created by Nadav on 12/31/2016.
 */
public class ToolReleaseTask extends Task<Void> {
    private Tool tool;
    private Warehouse storage;

    public ToolReleaseTask(Tool tool, Warehouse ware){
        this.tool = tool;
        this.storage = ware;
    }


    @Override
    protected void start() {
        storage.releaseTool(tool);
        complete(null);
    }
}
