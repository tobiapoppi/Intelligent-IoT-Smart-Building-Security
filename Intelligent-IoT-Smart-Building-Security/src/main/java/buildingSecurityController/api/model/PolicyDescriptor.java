package buildingSecurityController.api.model;

public class PolicyDescriptor {

    private String policy_id;
    private String area_id;

    private Boolean is_enabled; //to determine if in that zone the controls are taking place
    private Boolean presence_mode; //if 0 --> it counts the number of maxPersons; if 1 --> just the presence
    private String start_working_time;  //start working time for this policy
    private String end_working_time; //stop working time for this policy
    private Integer max_persons;

    public PolicyDescriptor(String policy_id, String area_id, Boolean is_enabled, Boolean presence_mode, String start_working_time, String end_working_time, Integer max_persons) {
        this.policy_id = policy_id;
        this.area_id = area_id;
        this.is_enabled = is_enabled;
        this.presence_mode = presence_mode;
        this.start_working_time = start_working_time;
        this.end_working_time = end_working_time;
        this.max_persons = max_persons;
    }

    public PolicyDescriptor(){

    }

    public String getPolicy_id() {
        return policy_id;
    }

    public void setPolicy_id(String policy_id) {
        this.policy_id = policy_id;
    }

    public String getArea_id() {
        return area_id;
    }

    public void setArea_id(String area_id) {
        this.area_id = area_id;
    }

    public Boolean getIs_enabled() {
        return is_enabled;
    }

    public void setIs_enabled(Boolean is_enabled) {
        this.is_enabled = is_enabled;
    }

    public Boolean getPresence_mode() {
        return presence_mode;
    }

    public void setPresence_mode(Boolean presence_mode) {
        this.presence_mode = presence_mode;
    }

    public String getStart_working_time() {
        return start_working_time;
    }

    public void setStart_working_time(String start_working_time) {
        this.start_working_time = start_working_time;
    }

    public String getEnd_working_time() {
        return end_working_time;
    }

    public void setEnd_working_time(String end_working_time) {
        this.end_working_time = end_working_time;
    }

    public Integer getMax_persons() {
        return max_persons;
    }

    public void setMax_persons(Integer max_persons) {
        this.max_persons = max_persons;
    }

    @Override
    public String toString() {
        return "PolicyDescriptor{" +
                "policy_id='" + policy_id + '\'' +
                ", location_id='" + area_id + '\'' +
                ", is_enabled=" + is_enabled +
                ", presence_mode=" + presence_mode +
                ", start_working_time='" + start_working_time + '\'' +
                ", end_working_time='" + end_working_time + '\'' +
                ", max_persons=" + max_persons +
                '}';
    }
}
