package com.teamacronymcoders.base.registry;

import com.teamacronymcoders.base.IBaseMod;
import com.teamacronymcoders.base.IModAware;
import com.teamacronymcoders.base.items.IHasRecipe;
import com.teamacronymcoders.base.registry.config.ConfigRegistry;
import com.teamacronymcoders.base.registry.config.IConfigListener;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Registry<T> {
    protected IBaseMod mod;
    protected Registry<T> instance;
    protected Map<String, T> entries = new HashMap<>();
    private LoadingStage loadingStage = LoadingStage.PREINIT;

    public Registry(IBaseMod mod) {
        this.mod = mod;
    }

    public void preInit() {
        this.entries.forEach((name, entry) -> {
            initiateEntry(name, entry);
            initiateModel(name, entry);
        });

        setLoadingStage(LoadingStage.INIT);
    }

    protected void initiateEntry(String name, T entry) {
        if (entry instanceof IConfigListener) {
            mod.getRegistryHolder().getRegistry(ConfigRegistry.class, "CONFIG").addListener((IConfigListener) entry);
        }
        if(entry instanceof IModAware) {
            ((IModAware) entry).setMod(mod);
        }
    }

    protected void initiateModel(String name, T entry) {
    }

    public void init() {
        this.entries.forEach((name, entry) -> initiateRecipes(entry));

        setLoadingStage(LoadingStage.POSTINIT);
    }

    public void postInit() {
        setLoadingStage(LoadingStage.DONE);
    }

    public void initiateRecipes(T entry) {
        if (entry instanceof IHasRecipe) {
            ((IHasRecipe) entry).getRecipes(new ArrayList<>()).forEach(GameRegistry::addRecipe);
        }
    }

    public T get(String name) {
        return this.entries.get(name);
    }

    public void register(String name, T entry) {
        if(getLoadingStage() != LoadingStage.PREINIT) {
            throw new UnsupportedOperationException("ALL REGISTERING MUST HAPPEN IN PREINIT");
        }
        this.entries.put(name, entry);
    }

    public LoadingStage getLoadingStage() {
        return loadingStage;
    }

    public void setLoadingStage(LoadingStage stage) {
        if (stage.ordinal() < loadingStage.ordinal()) {
            throw new IllegalArgumentException("LOADING STAGE SHOULD NEVER MOVE BACKWARDS");
        } else {
            this.loadingStage = stage;
        }
    }

    public Map<String, T> getEntries() {
        return this.entries;
    }
}
