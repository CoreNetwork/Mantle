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
        super(wither);
        stompMove = false;
        this.a(1);
    }

    @Override
    protected void initializeMoveConfig()
    {

    }

    public boolean a()
    {
        return super.a();
    }

    public boolean b()
    {
        return stompMove;
    }

    public void c()
    {
        MLog.debug("[WITHER] Starting Stomp");
        super.c();
        stompMove = true;


        //Get a copy of target list
        //fly 'away' from everyone
        //check if enough air around?
    }

    @Override
    public void d()
    {
        MLog.debug("[WITHER] Stoping Stomp");
        stompMove = false;
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
