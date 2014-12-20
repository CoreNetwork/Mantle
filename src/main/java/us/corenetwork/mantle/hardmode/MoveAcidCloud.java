package us.corenetwork.mantle.hardmode;

/**
 * Created by Ginaf on 2014-12-18.
 */
public class MoveAcidCloud extends AbstractWitherMove {

    public MoveAcidCloud(CustomWither wither)
    {
        super(wither, "Acid Cloud", "AC");
    }

    @Override
    protected void initializeMoveConfig()
    {


        MANA_COST = HardmodeSettings.WITHER_PH_SA_MANACOST.integer();
        COOLDOWN = HardmodeSettings.WITHER_PH_SA_COOLDOWN.integer();
        NORMAL_ATTACK = HardmodeSettings.WITHER_PH_SA_NORMALATTACK.bool();
    }

}
