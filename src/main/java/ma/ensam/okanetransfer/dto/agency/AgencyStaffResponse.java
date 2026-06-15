package ma.ensam.okanetransfer.dto.agency;

import java.util.ArrayList;
import java.util.List;
import ma.ensam.okanetransfer.dto.user.UserSummaryResponse;

public class AgencyStaffResponse {
    private Long agencyId;
    private String agencyCode;
    private String agencyName;
    private List<UserSummaryResponse> agents = new ArrayList<>();
    private List<UserSummaryResponse> managers = new ArrayList<>();

    public Long getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(Long agencyId) {
        this.agencyId = agencyId;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public List<UserSummaryResponse> getAgents() {
        return agents;
    }

    public void setAgents(List<UserSummaryResponse> agents) {
        this.agents = agents;
    }

    public List<UserSummaryResponse> getManagers() {
        return managers;
    }

    public void setManagers(List<UserSummaryResponse> managers) {
        this.managers = managers;
    }
}
