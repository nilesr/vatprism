package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.CompositeCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import net.marvk.fs.vatsim.map.data.*;

public class MainViewModel implements ViewModel {
    private final Command loadAirports;
    private final Command loadFirs;
    private final Command loadFirbs;
    private final Command loadUirs;
    private final Command loadClients;
    private final NotificationCenter notificationCenter;
    private final Command loadInternationalDateLine;

    @Inject
    public MainViewModel(
            final AirportRepository airportRepository,
            final ClientRepository clientRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final NotificationCenter notificationCenter
    ) {
        loadAirports = new ReloadRepositoryAction(airportRepository).asCommand();
        loadInternationalDateLine = new ReloadRepositoryAction(internationalDateLineRepository).asCommand();
        loadFirbs = new ReloadRepositoryAction(flightInformationRegionBoundaryRepository).asCommand();
        loadFirs = new ReloadRepositoryAction(flightInformationRegionRepository).asCommand();
        loadUirs = new ReloadRepositoryAction(upperInformationRegionRepository).asCommand();
        loadClients = new ReloadRepositoryAction(clientRepository).asCommand();
        this.notificationCenter = notificationCenter;

        new CompositeCommand(
                loadAirports,
                loadInternationalDateLine,
                loadFirbs,
                loadFirs,
                loadUirs,
                loadClients
        ).execute();

        notificationCenter.subscribe("RELOAD_CLIENTS", (key, payload) -> {
            loadClients.execute();
            notificationCenter.publish("REPAINT");
        });
    }

    private static final class ReloadRepositoryAction extends Action {
        private final Repository<?> repository;

        private ReloadRepositoryAction(final Repository<?> repository) {
            this.repository = repository;
        }

        @Override
        protected void action() throws RepositoryException {
            repository.reload();
        }

        private DelegateCommand asCommand() {
            return new DelegateCommand(() -> this, false);
        }
    }
}
