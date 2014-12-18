package us.corenetwork.mantle.hardmode;

import us.corenetwork.mantle.MLog;

/**
 * Created by Ginaf on 2014-12-10.
 */
public class MoveStomp extends AbstractWitherMove {


    private boolean stompMove;
    private int onGroundTime = 0;

    public MoveStomp(CustomWither wither)
    {
        super(wither, "Stomp", "St");
        this.a(3);
    }

    @Override
    protected void initializeMoveConfig()
    {

    }

    @Override
    public void e()
    {

        //pick target, one by one from the copy of target list
        //fly towards it
        //either just set TARGET to follow player, or do some weird movements here and there, probalby leave that second thing for much much later
        //as it is mostly visual
        //TARGET has to be way above player
        //turn wither target OFF, which will turn his movements off,
        //mot Y x10, dropping the guy, apply dmg and all the things
        //wait
        //fly up, repeat process
        wither.motY *= 2;

        if (wither.onGround)
        {
            onGroundTime++;
            if (onGroundTime == 100)
            {
                stompMove = false;
                onGroundTime = 0;
            }
        }
    }



}
