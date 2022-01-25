package com.bonn2.modules.uncaps;

import com.bonn2.Bot;
import com.bonn2.modules.Module;

public class UnCaps extends Module {
    @Override
    public void load() {
        version = "v1.0";
        System.out.println("Loading UnCaps module " + version + "...");
        System.out.println("- Registering Listeners...");
        Bot.jda.addEventListener(new UnCapsListener());
    }
}
