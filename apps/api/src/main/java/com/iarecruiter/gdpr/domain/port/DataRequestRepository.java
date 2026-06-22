package com.iarecruiter.gdpr.domain.port;

import com.iarecruiter.gdpr.domain.model.DataRequest;
import java.util.UUID;

public interface DataRequestRepository {
    DataRequest save(DataRequest dataRequest);
}
