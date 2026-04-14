package com.freelanceflow.aiquery.tools;

import com.freelanceflow.client.ClientService;
import com.freelanceflow.client.dto.ClientResponse;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service("getClientsTool")
@Description("Fetch all clients for the current user.")
public class GetClientsTool implements Function<Long, List<ClientResponse>> {

    private final ClientService clientService;

    public GetClientsTool(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public List<ClientResponse> apply(Long userId) {
        // Simple unpaged wrapper for AI context
        return clientService.listAll(userId, Pageable.unpaged()).getContent();
    }
}
