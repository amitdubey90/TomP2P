package net.tomp2p.futures;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import net.tomp2p.peers.PeerAddress;

public class FutureDiscover extends BaseFutureImpl
{
	final private Timer timer = new HashedWheelTimer(10, TimeUnit.MILLISECONDS, 10);
	final private Timeout timeout;
	//
	private boolean goodUDP;
	private boolean goodTCP;
	private PeerAddress peerAddress;

	public FutureDiscover(int delaySec)
	{
		timeout = timer.newTimeout(new DiscoverTimeoutTask(), delaySec, TimeUnit.SECONDS);
	}

	@Override
	public void cancel()
	{
		timeout.cancel();
		timer.stop();
		super.cancel();
	}

	public void setGoodUDP(boolean goodUDP)
	{
		synchronized (lock)
		{
			this.goodUDP = goodUDP;
		}
	}

	public boolean getGoodUPD()
	{
		synchronized (lock)
		{
			return goodUDP;
		}
	}
	
	public void setGoodTCP(boolean goodTCP)
	{
		synchronized (lock)
		{
			this.goodTCP=goodTCP;
		}
	}

	public boolean getGoodTCP()
	{
		synchronized (lock)
		{
			return goodTCP;
		}
	}

	public void done(PeerAddress peerAddress)
	{
		timer.stop();
		synchronized (lock)
		{
			if (!setCompletedAndNotify())
				return;
			this.peerAddress = peerAddress;
		}
		notifyListerenrs();
	}

	public PeerAddress getPeerAddress()
	{
		synchronized (lock)
		{
			return peerAddress;
		}
	}
	private final class DiscoverTimeoutTask implements TimerTask
	{
		@Override
		public void run(Timeout timeout) throws Exception
		{
			timer.stop();
			setFailed("Timeout");
		}
	}
}