package com.incidentiq.ai;

/**
 * Provider-agnostic abstraction over whatever LLM produces the investigation.
 * The rest of the application depends only on this interface, never on a
 * specific vendor SDK, so the underlying provider can be swapped by adding a
 * new implementation and changing configuration only.
 *
 * Implementations MUST:
 *  - Return structured, schema-validated output (see AIInvestigationResponse).
 *  - Base conclusions only on the evidence supplied in the request.
 *  - Report low/zero confidence rather than inventing an answer when evidence
 *    is insufficient.
 *  - Never leak internal chain-of-thought; reasoning is expressed only as
 *    auditable evidence references.
 */
public interface AIInvestigationService {

    AIInvestigationResponse investigate(AIInvestigationRequest request);

    /** Identifier stored with each saved report so its origin is auditable. */
    String providerName();
}
