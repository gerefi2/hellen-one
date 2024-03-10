package com.gerefi.enums;
//was generated automatically by gerefi tool config_definition_base.jar based on StateDictionaryGenerator integration/LiveData.yaml on Wed Jan 03 03:26:05 UTC 2024n
import com.gerefi.config.generated.*;
import com.gerefi.ldmp.StateDictionary;

public class StateDictionaryFactory {
    public static void initialize(StateDictionary stateDictionary) {
        stateDictionary.register(live_data_e.LDS_output_channels, TsOutputs.VALUES, "status_loop");
        stateDictionary.register(live_data_e.LDS_fuel_computer, FuelComputer.VALUES, "fuel_computer");
        stateDictionary.register(live_data_e.LDS_ignition_state, IgnitionState.VALUES, "advance_map");
        stateDictionary.register(live_data_e.LDS_knock_controller, KnockController.VALUES, "knock_controller");
        stateDictionary.register(live_data_e.LDS_tcu_controller, TcuController.VALUES, "tcu_controller");
        stateDictionary.register(live_data_e.LDS_throttle_model, ThrottleModel.VALUES, "throttle_model");
        stateDictionary.register(live_data_e.LDS_high_pressure_fuel_pump, HighPressureFuelPump.VALUES, "high_pressure_fuel_pump");
        stateDictionary.register(live_data_e.LDS_injector_model, InjectorModel.VALUES, "injector_model");
        stateDictionary.register(live_data_e.LDS_launch_control_state, LaunchControl.VALUES, "launch_control");
        stateDictionary.register(live_data_e.LDS_antilag_system_state, AntilagSystem.VALUES, "antilag_system");
        stateDictionary.register(live_data_e.LDS_boost_control, BoostControl.VALUES, "boost_control");
        stateDictionary.register(live_data_e.LDS_ac_control, AcControl.VALUES, "ac_control");
        stateDictionary.register(live_data_e.LDS_fan_control, FanControl.VALUES, "fan_control");
        stateDictionary.register(live_data_e.LDS_fuel_pump_control, FuelPump.VALUES, "fuel_pump");
        stateDictionary.register(live_data_e.LDS_main_relay, MainRelay.VALUES, "main_relay");
        stateDictionary.register(live_data_e.LDS_engine_state, EngineState.VALUES, "engine");
        stateDictionary.register(live_data_e.LDS_tps_accel_state, TpsAccelState.VALUES, "accel_enrichment");
        stateDictionary.register(live_data_e.LDS_trigger_central, TriggerCentral.VALUES, "trigger_central");
        stateDictionary.register(live_data_e.LDS_trigger_state, TriggerState.VALUES, "trigger_decoder");
        stateDictionary.register(live_data_e.LDS_trigger_state_primary, TriggerStatePrimary.VALUES, "trigger_decoder");
        stateDictionary.register(live_data_e.LDS_wall_fuel_state, WallFuelState.VALUES, "wall_fuel");
        stateDictionary.register(live_data_e.LDS_idle_state, IdleState.VALUES, "idle_thread");
        stateDictionary.register(live_data_e.LDS_electronic_throttle, ElectronicThrottle.VALUES, "electronic_throttle");
        stateDictionary.register(live_data_e.LDS_wideband_state, WidebandController.VALUES, "AemXSeriesLambda");
        stateDictionary.register(live_data_e.LDS_dc_motors, DcMotors.VALUES, "dc_motors");
        stateDictionary.register(live_data_e.LDS_sent_state, SentState.VALUES, "sent");
        stateDictionary.register(live_data_e.LDS_vvt, VvtState.VALUES, "vvt");
        stateDictionary.register(live_data_e.LDS_lambda_monitor, LambdaMonitor.VALUES, "lambda_monitor");
    }
}