package ru.malnev.gbcloud.client.conversations;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.malnev.gbcloud.client.events.ELsConversationComplete;
import ru.malnev.gbcloud.common.conversations.AbstractConversation;
import ru.malnev.gbcloud.common.messages.IMessage;
import ru.malnev.gbcloud.common.messages.LsRequest;
import ru.malnev.gbcloud.common.messages.LsResponse;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class LsClientAgent extends AbstractConversation
{
    @Getter
    @Setter
    private String requestedPath;

    @Inject
    private Event<ELsConversationComplete> lsConversationCompleteBus;

    @Override
    public void processMessageFromPeer(@NotNull IMessage message)
    {
        if (message instanceof LsResponse)
        {
            lsConversationCompleteBus.fireAsync(new ELsConversationComplete(this, (LsResponse) message));
        }
    }

    @Override
    public void start()
    {
        setTimeoutMillis(60000);
        expectMessage(LsResponse.class);
        final LsRequest outgoingMessage = new LsRequest(requestedPath);
        sendMessageToPeer(outgoingMessage);
    }

    @Override
    public void stop()
    {

    }
}